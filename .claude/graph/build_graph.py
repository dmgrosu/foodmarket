#!/usr/bin/env python3
"""Reconcile .claude/graph/*.md against the source tree.

The .md files hold curated columns (api surfaces, prose details, notes) that no
regex can author. What a regex CAN author is the structural skeleton: which
types exist, which routes are mapped, which dependencies are injected, which
tables are declared. This script extracts that skeleton from source and diffs it
against the committed graph, so a rename, a new use case, a new endpoint, a new
injection or a schema edit all surface as a precise, actionable mismatch.

  build_graph.py            reconcile and print a report (exit 0 always)
  build_graph.py --check    reconcile; exit 1 if the graph is stale
  build_graph.py --verify   structural self-checks (coverage, counts, refs)
  build_graph.py --json     emit the node/edge graph parsed from the .md files

Deliberately free of false positives: it compares sets of identifiers, never
file contents or timestamps, so whitespace and comment edits never trip it.
Python 3 stdlib only, no Java parser, no third-party deps.
"""

import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
GRAPH = os.path.join(ROOT, ".claude", "graph")
JAVA_MAIN = os.path.join(ROOT, "src", "main", "java")
JAVA_TEST = os.path.join(ROOT, "src", "test", "java")
FRONTEND = os.path.join(ROOT, "frontend", "src")
PROD_SQL = os.path.join(ROOT, "src", "main", "resources", "create_db.sql")
TEST_SQL = os.path.join(ROOT, "src", "test", "resources", "schema.sql")
PKG_PREFIX = "src/main/java/md/ramaiana/foodmarket/"

# Targets that legitimately have no row in nodes.md: framework supertypes.
# A `private final X y;` field counts as dependency injection only when X is one
# of these; on an entity the same syntax is just a typed value field.
BEAN_KINDS = {"usecase", "repo", "voter", "service", "config", "abstract"}

EXTERNAL_TYPES = {
    "CrudRepository", "ListCrudRepository", "PagingAndSortingRepository",
    "OncePerRequestFilter", "UserDetails", "UserDetailsService",
}


# --------------------------------------------------------------------------
# reading the committed graph
# --------------------------------------------------------------------------

def rows(filename, ncols):
    """Yield data rows of a markdown table as lists of stripped cells."""
    path = os.path.join(GRAPH, filename)
    out = []
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            line = line.rstrip("\n")
            if not line.startswith("| ") or not line.endswith(" |"):
                continue
            if set(line) <= set("|- "):
                continue
            cells = [c.strip() for c in line[2:-2].split(" | ")]
            if len(cells) == ncols:
                out.append(cells)
    return out


def graph_nodes():
    return [r for r in rows("nodes.md", 5) if r[0] != "id"]


def graph_edges():
    return [r for r in rows("edges.md", 4) if r[0] != "from"]


def graph_routes():
    return [r for r in rows("routes.md", 9)
            if r[0] in ("GET", "POST", "PUT", "DELETE")]


def graph_frontend():
    return [r for r in rows("frontend.md", 5)
            if r[1] in ("GET", "POST", "PUT", "DELETE")]


def graph_tables():
    return [r for r in rows("schema.md", 7) if r[0] != "table"]


# --------------------------------------------------------------------------
# extraction from source
# --------------------------------------------------------------------------

def java_files(base):
    for dirpath, _, names in os.walk(base):
        for name in names:
            if name.endswith(".java"):
                yield os.path.join(dirpath, name)


def read(path):
    with open(path, encoding="utf-8") as fh:
        return fh.read()


RE_FIELD = re.compile(r"^\s*private\s+final\s+([A-Z][A-Za-z0-9_]*)"
                      r"(?:<[^>]*>)?\s+([a-z][A-Za-z0-9_]*)\s*;", re.M)
RE_TABLE = re.compile(r'@Table\(\s*"([^"]+)"\s*\)')
RE_CLASS_MAP = re.compile(r'@RequestMapping\(\s*"([^"]+)"\s*\)')
RE_METHOD_MAP = re.compile(r'@(Get|Post|Put|Delete)Mapping(?:\(\s*"([^"]*)"\s*\))?')
RE_HANDLER = re.compile(r"^\s*public\s+[\w<>,\[\]\s.?]+?\s+([a-zA-Z_]\w*)\s*\(", re.M)
RE_VOTER_CALL = re.compile(r"accessVoter\.(assertCan\w+)\s*\(")
RE_UC_CALL = re.compile(r"\b([a-z]\w*UseCase)\.(\w+)\s*\(")
RE_MAPPED = re.compile(r"@MappedCollection\([^)]*\)\s*(?:private\s+)?(?:final\s+)?"
                       r"(?:Set|List)<(\w+)>")
RE_AGGREF = re.compile(r"AggregateReference<\s*(\w+)\s*,")
RE_AXIOS = re.compile(r"axios(?:Instance|_instance)?\.(get|post|put|delete)\s*\(")


def extract_types():
    """{simple name: dir relative to repo root} for every Java type."""
    found = {}
    for base in (JAVA_MAIN, JAVA_TEST):
        if not os.path.isdir(base):
            continue
        for path in java_files(base):
            name = os.path.basename(path)[:-5]
            rel = os.path.relpath(os.path.dirname(path), ROOT)
            found[name] = rel
    return found


def extract_injections():
    """{(OwnerType, InjectedType)} from `private final X y;` declarations."""
    edges = set()
    for path in java_files(JAVA_MAIN):
        owner = os.path.basename(path)[:-5]
        for typ, _ in RE_FIELD.findall(read(path)):
            edges.add((owner, typ))
    return edges


def extract_tables():
    """{EntityType: table name} from @Table."""
    out = {}
    for path in java_files(JAVA_MAIN):
        m = RE_TABLE.search(read(path))
        if m:
            out[os.path.basename(path)[:-5]] = m.group(1)
    return out


def extract_structural_edges(kinds):
    """embeds and fk edges, as {(from, rel, to)}.

    AggregateReference also appears in use-case method signatures, where it is a
    lookup argument rather than a foreign key, so fk is restricted to entities.
    """
    edges = set()
    for path in java_files(JAVA_MAIN):
        owner = os.path.basename(path)[:-5]
        src = read(path)
        for child in RE_MAPPED.findall(src):
            edges.add((owner, "embeds", child))
        if kinds.get(owner) == "entity":
            for target in RE_AGGREF.findall(src):
                edges.add((owner, "fk", target))
    return edges


def extract_routes():
    """[(METHOD, full path, Controller#handler, VoterType#assert, UseCaseType#method)]"""
    out = []
    for path in java_files(JAVA_MAIN):
        if not path.endswith("Controller.java"):
            continue
        cls = os.path.basename(path)[:-5]
        src = read(path)
        base = ""
        m = RE_CLASS_MAP.search(src)
        if m:
            base = m.group(1)
        # field name -> declared type, so we can report types not variable names
        fields = {name: typ for typ, name in RE_FIELD.findall(src)}
        for match in RE_METHOD_MAP.finditer(src):
            verb = match.group(1).upper()
            sub = match.group(2) or ""
            tail = src[match.end():]
            hm = RE_HANDLER.search(tail)
            handler = hm.group(1) if hm else "?"
            # body = from the handler signature to the next mapping annotation
            nxt = RE_METHOD_MAP.search(tail)
            body = tail[: nxt.start()] if nxt else tail
            vm = RE_VOTER_CALL.search(body)
            voter = ""
            if vm:
                voter = "%s#%s" % (fields.get("accessVoter", "?"), vm.group(1))
            um = RE_UC_CALL.search(body)
            usecase = ""
            if um:
                usecase = "%s#%s" % (fields.get(um.group(1), um.group(1)), um.group(2))
            out.append((verb, base + sub, "%s#%s" % (cls, handler), voter, usecase))
    return out


def extract_axios_count():
    n = 0
    for dirpath, dirs, names in os.walk(FRONTEND):
        dirs[:] = [d for d in dirs if d != "node_modules"]
        for name in names:
            if name.endswith(".js"):
                n += len(RE_AXIOS.findall(read(os.path.join(dirpath, name))))
    return n


def extract_sql_columns(path):
    """{table: {column names}} from CREATE TABLE blocks."""
    if not os.path.exists(path):
        return {}
    src = re.sub(r"--[^\n]*", "", read(path))
    out = {}
    for m in re.finditer(r"create\s+table\s+(?:if\s+not\s+exists\s+)?"
                         r"([\"\w.]+)\s*\((.*?)\n\s*\)\s*;", src,
                         re.S | re.I):
        table = m.group(1).split(".")[-1].strip('"')
        cols = set()
        depth = 0
        current = ""
        for ch in m.group(2):
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
            if ch == "," and depth == 0:
                cols.add(current)
                current = ""
            else:
                current += ch
        cols.add(current)
        names = set()
        for c in cols:
            c = c.strip()
            if not c:
                continue
            first = c.split()[0].strip('"').lower()
            if first in ("primary", "foreign", "unique", "constraint", "check", "key"):
                continue
            names.add(first)
        out[table] = names
    return out


# --------------------------------------------------------------------------
# reconciliation
# --------------------------------------------------------------------------

def reconcile():
    problems = []
    kinds = {r[0]: r[1] for r in graph_nodes()}

    # 1. node coverage
    types = extract_types()
    node_ids = {r[0] for r in graph_nodes()}
    for name in sorted(set(types) - node_ids):
        problems.append("nodes.md: missing row for %s (%s)" % (name, types[name]))
    java_dirs = {r[0]: r[2] for r in graph_nodes()
                 if not r[2].startswith("frontend/")}
    for nid, d in sorted(java_dirs.items()):
        if nid not in types:
            problems.append("nodes.md: row %s has no source file" % nid)
            continue
        actual = types[nid]
        expect = d if d.startswith("src/") else (PKG_PREFIX + d).rstrip("/")
        if actual.rstrip("/") != expect:
            problems.append("nodes.md: %s dir is %s, source is at %s"
                            % (nid, d, actual))

    # 2. routes
    src_routes = {(v, p, h) for v, p, h, _, _ in extract_routes()}
    md_routes = {(r[0], r[1], r[2]) for r in graph_routes()}
    for r in sorted(src_routes - md_routes):
        problems.append("routes.md: missing endpoint %s %s -> %s" % r)
    for r in sorted(md_routes - src_routes):
        problems.append("routes.md: stale endpoint %s %s -> %s" % r)

    md_by_key = {(r[0], r[1]): r for r in graph_routes()}
    for verb, path, handler, voter, usecase in extract_routes():
        row = md_by_key.get((verb, path))
        if not row:
            continue
        if row[3] != voter:
            problems.append("routes.md: %s %s voter is '%s', source has '%s'"
                            % (verb, path, row[3], voter))
        if row[4] != usecase:
            problems.append("routes.md: %s %s usecase is '%s', source has '%s'"
                            % (verb, path, row[4], usecase))

    # 3. injection edges
    src_inj = extract_injections()
    md_inj = {(r[0], r[2]) for r in graph_edges() if r[1] == "injects"}
    for a, b in sorted(src_inj - md_inj):
        # framework types have no node; enum/value fields on entities are not
        # dependencies, only bean-kinded targets count as an injection
        if kinds.get(b) in BEAN_KINDS:
            problems.append("edges.md: missing injects %s -> %s" % (a, b))
    for a, b in sorted(md_inj - src_inj):
        problems.append("edges.md: stale injects %s -> %s" % (a, b))

    # 4. @Table edges
    src_tbl = extract_tables()
    md_tbl = {r[0]: r[2] for r in graph_edges() if r[1] == "table"}
    for ent, tbl in sorted(src_tbl.items()):
        if md_tbl.get(ent) != tbl:
            problems.append("edges.md: %s table is '%s', source has '%s'"
                            % (ent, md_tbl.get(ent, "<missing>"), tbl))

    # 5. embeds / fk
    src_struct = extract_structural_edges(kinds)
    md_struct = {(r[0], r[1], r[2]) for r in graph_edges()
                 if r[1] in ("embeds", "fk")}
    for e in sorted(src_struct - md_struct):
        problems.append("edges.md: missing %s %s -> %s" % (e[1], e[0], e[2]))

    # 6. frontend call-site count
    n_axios = extract_axios_count()
    n_rows = len(graph_frontend())
    if n_axios != n_rows:
        problems.append("frontend.md: %d rows but %d axios call sites in source"
                        % (n_rows, n_axios))

    # 7. schema tables and columns
    prod = extract_sql_columns(PROD_SQL)
    test = extract_sql_columns(TEST_SQL)
    md_tables = {r[0] for r in graph_tables()}
    for t in sorted(set(prod) - md_tables):
        problems.append("schema.md: create_db.sql declares table '%s' with no row" % t)
    for t in sorted(md_tables - set(prod)):
        problems.append("schema.md: row for table '%s' not in create_db.sql" % t)

    drift = set()
    for t in sorted(set(prod) | set(test)):
        if t not in prod or t not in test:
            drift.add("%s.*" % t)
            continue
        for c in sorted(prod[t] ^ test[t]):
            drift.add("%s.%s" % (t, c))
    md_drift = {r[0] for r in rows("schema.md", 4) if r[3] != "status"}
    for d in sorted(drift - md_drift):
        problems.append("schema.md: undocumented drift at %s" % d)

    return problems


def verify():
    """Structural self-checks that do not depend on source extraction."""
    problems = []
    nodes = graph_nodes()
    ids = [r[0] for r in nodes]
    for nid in sorted({i for i in ids if ids.count(i) > 1}):
        problems.append("nodes.md: duplicate id %s" % nid)

    kinds = {"controller", "voter", "usecase", "entity", "repo", "request",
             "response", "dto", "enum", "exception", "config", "service",
             "annotation", "abstract", "util", "app", "fe-action", "fe-component",
             "fe-reducer", "fe-util"}
    for r in nodes:
        if r[1] not in kinds:
            problems.append("nodes.md: %s has unknown kind '%s'" % (r[0], r[1]))

    rels = {"injects", "calls", "guards", "extends", "implements", "embeds",
            "fk", "table", "throws", "scheduled"}
    known = set(ids) | EXTERNAL_TYPES
    tables = {r[2] for r in graph_edges() if r[1] == "table"}
    for r in graph_edges():
        if r[1] not in rels:
            problems.append("edges.md: unknown rel '%s' in %s -> %s"
                            % (r[1], r[0], r[2]))
        if r[0] not in known:
            problems.append("edges.md: unknown source node '%s'" % r[0])
        target = r[2]
        if r[1] == "guards":
            if target.split("#")[0] not in known:
                problems.append("edges.md: guards target '%s' unknown" % target)
        elif r[1] == "table":
            continue
        elif target not in known and target not in tables:
            problems.append("edges.md: unknown target node '%s'" % target)

    n_routes = len(graph_routes())
    if n_routes != 22:
        problems.append("routes.md: %d rows, expected 22" % n_routes)

    for r in graph_routes():
        for repo in filter(None, r[7].split(",")):
            if repo not in known:
                problems.append("routes.md: %s %s names unknown repo '%s'"
                                % (r[0], r[1], repo))
    return problems


def as_json():
    return {
        "nodes": [dict(zip(("id", "kind", "dir", "api", "note"), r))
                  for r in graph_nodes()],
        "edges": [dict(zip(("from", "rel", "to", "detail"), r))
                  for r in graph_edges()],
        "routes": [dict(zip(("method", "path", "handler", "voter", "usecase",
                             "request", "response", "repos", "frontend"), r))
                   for r in graph_routes()],
    }


def main():
    args = set(sys.argv[1:])
    quiet = "--quiet" in args

    if "--json" in args:
        json.dump(as_json(), sys.stdout, indent=2)
        print()
        return 0

    problems = verify() if "--verify" in args else reconcile()
    label = "verify" if "--verify" in args else "reconcile"

    if problems:
        if not quiet:
            print("Graph %s FAILED (%d problem%s):"
                  % (label, len(problems), "" if len(problems) == 1 else "s"))
            for p in problems:
                print("  - %s" % p)
            print("\nFix by editing the affected file(s) in .claude/graph/ to "
                  "match the source.")
        return 1 if ("--check" in args or "--verify" in args) else 0

    if not quiet:
        print("Graph %s OK." % label)
    return 0


if __name__ == "__main__":
    sys.exit(main())
