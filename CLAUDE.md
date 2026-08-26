# foodmarket

Spring Boot 4.0 / Java 25 / Maven backend + a separate CRA React 17 frontend (`frontend/`, not bundled into the jar).
Persistence is **Spring Data JDBC, not JPA**. Schema is hand-managed in two SQL files — **no Flyway, no Liquibase**.
Root package `md.ramaiana.foodmarket`. 7 domains, 15 HTTP endpoints, 13 tables.

## Find things in `.claude/graph/` — do not explore

| I need | Open | Cost |
|---|---|---|
| **anything about a named symbol** | `grep -rn "<Name>" .claude/graph/` | ~150 tokens, 1 call |
| what an endpoint does end-to-end | `.claude/graph/routes.md` | ~1k, read whole |
| where a class lives, what it exposes | `.claude/graph/nodes.md` | **grep, never read whole** (~3.4k) |
| who depends on X / coupling points | `.claude/graph/edges.md` — `grep CROSS-DOMAIN` for coupling | ~2k |
| tables, columns, prod/test drift | `.claude/graph/schema.md` | ~2.2k |
| which frontend code calls which endpoint | `.claude/graph/frontend.md` | ~800 |
| how to write new code here | `.claude/graph/conventions.md` | ~1.5k |
| known defects (don't re-discover them) | `.claude/graph/known-issues.md` | ~1.5k |

These files are **checked against source**, not authored by hand from scratch. After changing code, run:

```bash
python3 .claude/graph/build_graph.py --check
```

It extracts the structural skeleton from source — types, endpoints, injections, `@Table`s, SQL columns — and names exactly what drifted. Edit the affected `.md` to match, then re-run until clean. `--verify` runs the internal consistency checks (id uniqueness, closed `rel` vocabulary, no dangling references). A `SessionStart` hook runs `--check` automatically and prints the report; it never blocks.

## Row schemas

A grep hit arrives without its header, so the column order lives here.
`$J` = `src/main/java/md/ramaiana/foodmarket/`. A `dir` cell omits `$J` and the filename; the file is `<id>.java`.

- **nodes.md** — `| id | kind | dir | api | note |` — `note` is populated only for deviations.
- **edges.md** — `| from | rel | to | detail |` — `rel` ∈ `injects calls guards extends implements embeds fk table throws scheduled`. A `detail` starting `CROSS-DOMAIN` or `CROSS-LAYER` marks coupling. `extends`/`implements` may target a framework type absent from `nodes.md`.
- **routes.md** — `| method | path | controller#handler | voter#assert | usecase#method | request | response | repos | frontend |` — empty `voter` means no authorization check; empty `frontend` means no consumer.
- **schema.md** — A: `| table | entity | repo | pk | columns | soft-delete | notes |` · B: `| table.column | create_db.sql | test/schema.sql | status |`
- **frontend.md** — `| caller | http | backend path | params | dispatches |`

## Invariants

1. Domains are strictly 3-slice: `core/{request,response,usecase}` · `data` · `presentation/{controller,voter}`. `auth` has a fourth `core/handler` slice — see invariant 8.
2. One public `execute(...)` per use case, annotated `@UseCase`, constructor-injected `final` fields — except a use case that mixes a database write with an external call, which splits into `preExecute`/`executeTransactionalEffect`/`executeSideEffects` (invariant 8).
3. One `assertCanX()` per controller handler, named 1:1 with it.
4. Controller handlers are exactly two lines: `accessVoter.assertCanX();` then `return useCase.execute(...);` (or `return xRequestHandler.handle(...);` for the handler-backed endpoints in invariant 8).
5. Soft delete is a nullable `deletedAt` plus `...AndDeletedAtIsNull` finders. Never hard-delete.
6. **Any schema change edits BOTH `src/main/resources/create_db.sql` and `src/test/resources/schema.sql`.** They still drift in ~40 columns — read `schema.md` Table B first.
7. New DTOs are `record`s with an entity-copy constructor.
8. **A transaction must never span an external call.** A use case that needs both a database write and a call to an external system (email, a third-party API) splits into three phases and is sequenced by a `@RequestHandler` in `core/handler`, using a `thisProxy` self-injection so the write commits in its own transaction before the external call runs. See `.claude/graph/conventions.md` for the exact shape — `domain/auth/core/handler/AuthRegisterRequestHandler` is the reference example.
   **A handler is only justified by that split.** A use case with no external call keeps one self-managing `@Transactional execute(...)` and is called straight from the controller — adding a handler that only delegates makes the layer meaningless as a signal. `RegistrationConfirmUseCase` is the reference for that side.

## The installed scaffolding skills do not match this repo

`/new-entity`, `/new-usecase`, `/new-endpoint`, `/new-migration` generate **JPA entities, Flyway migrations, `Specification` queries, and a `RequestHandler` layer shaped for a different (JPA-based) codebase**. This repo has none of the JPA/Flyway/Specification parts, and its own `RequestHandler` layer (invariant 8) is narrower and Spring-Data-JDBC-shaped — don't reach for those skills' version of it. Follow `.claude/graph/conventions.md` instead of those templates.

## Commands

```bash
./mvnw -q test
```

```bash
cd frontend && npm start
```

```bash
python3 .claude/graph/build_graph.py --check
```
