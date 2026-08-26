# conventions.md — how to write code in this repo

HAND-WRITTEN. Not regenerated. Update when a pattern genuinely changes.

## Package layout — strict 3-slice per domain

```
domain/<name>/core/{request,response,usecase}    business logic + DTOs
domain/<name>/data                                entities + repositories
domain/<name>/presentation/{controller,voter}     HTTP + authorization
```

`price` has only `core/response` + `data` (value-object domain, no HTTP). `auth` adds a fourth sub-package `presentation/service` for the Spring Security `UserDetailsService` SPI adapter, and a fifth `core/handler` for `RequestHandler`s (see below). No `core/adapter` package exists in any domain.

Non-domain code: `config/` (4 classes), `shared/{annotation,dataexchange,enums,exception/http,schedule,util}`.

## Naming

Suffixes are load-bearing: `*Controller`, `*AccessVoter`, `*UseCase`, `*RequestHandler`, `*Entity`, `*Repository`, `*Request`, `*Response`, `Erp*Dto`.

Use cases are **noun-first, verb-last** — `<Domain|Noun><Verb>UseCase`:
`OrderAddProductUseCase`, `ProductFindByErpCodeUseCase`, `RegistrationConfirmUseCase`,
`RegistrationTokenIssueUseCase` — never `AddProductToOrderUseCase` or `AuthConfirmRegistrationUseCase`
(verb stranded in the middle). Pick the noun the use case actually operates on, so it lines up with the
entity/repository/table it touches: `RegistrationTokenIssueUseCase` writes `RegistrationTokenEntity`
into `registration_token`. Request handlers and their request/response DTOs take the same noun as the
use case they front (`RegistrationConfirmRequestHandler`, `RegistrationConfirmRequest`).

Names must say what the thing is without a comment. Config properties in particular: prefer
`confirmationLinkValidityHours` over `tokenTtlHours`, and never reuse one name for two things —
`confirmationPageUrl` (the page, from config) and `confirmationUrl` (that page plus the token) are
deliberately distinct.

Values passed between the phases of a three-phase use case are `record`s **nested inside the use case
that produces them**, named after the phase — e.g.
`RegistrationTokenIssueUseCase.TransactionalEffectResult`. They are internal plumbing, never bound
from or to HTTP, so they do not live in `core/request` or `core/response`.

## UseCase shape

`@UseCase` (a `@Component` meta-annotation at `shared/annotation/UseCase.java`) + `@RequiredArgsConstructor`, all fields `final`, **one public `execute(...)` method**.
`@Transactional(readOnly = true)` for reads, `@Transactional(rollbackFor = Exception.class)` for writes.

**Exception — a use case that combines a database write with a call to an external system** (email, a
third-party API) does not get a single `execute()`. It splits into three phases and is sequenced by a
`RequestHandler` (below), never called directly by a controller:

- `preExecute(...)` — validation that can fail before any transaction is held. No `@Transactional`.
- `executeTransactionalEffect(...)` — the database write. `@Transactional(propagation = Propagation.MANDATORY)`:
  it refuses to run without a caller-provided transaction, so it can never quietly open its own.
- `executeSideEffects(...)` — the external call, run only after the write has committed.
  `@Transactional(propagation = Propagation.NEVER)`: it **throws** if a transaction is active, which is
  what actually enforces "never call an external system inside a transaction" — see
  `domain/auth/core/usecase/AuthRegisterUseCase.java` and its sibling `RegistrationConfirmationResendUseCase`.
  `MailjetAdapter` carries the same `Propagation.NEVER` at the class level for the same reason.

A use case that is *only* a database write still gets a single `execute()`. A use case that is *only* an
external call (e.g. `RegistrationConfirmationMailUseCase`) also gets a single `execute()`, annotated
`Propagation.NEVER`, and is called directly from `executeSideEffects` — it does not need its own handler.

## RequestHandler shape

`@RequestHandler` (a `@Component` meta-annotation at `shared/annotation/RequestHandler.java`, identical
in shape to `@UseCase`) + `@RequiredArgsConstructor`. Exists only for endpoints backed by a three-phase
use case; a use case with a single `execute()` is still called directly from the controller.

```java
@RequestHandler
@RequiredArgsConstructor
public class XRequestHandler {

  // Use cases
  private final XUseCase xUseCase;

  // Proxies
  @Lazy @Autowired private XRequestHandler thisProxy;

  @NonNull
  public XResponse handle(@NonNull XRequest request) {
    xUseCase.preExecute(request);
    Effect effect = thisProxy.persist(request);           // tx opens AND COMMITS here
    xUseCase.executeSideEffects(effect);                   // runs only after that commit
    return new XResponse(effect);
  }

  @NonNull
  @Transactional(readOnly = false, rollbackFor = Exception.class)
  protected Effect persist(@NonNull XRequest request) {
    return xUseCase.executeTransactionalEffect(request);
  }
}
```

**Only a genuine phase split earns a handler.** With no external call there is nothing to sequence, so the use case keeps a single self-managing `@Transactional(rollbackFor = Exception.class) execute(...)` and the controller calls it directly (`RegistrationConfirmUseCase`). A handler that only delegates adds a layer and removes the signal that the layer exists to carry.

`thisProxy` (`@Lazy @Autowired` on one line, to break the self-reference cycle) lives on the **handler**,
never on the use case. `handle()` must call `thisProxy.persist(...)`, not `persist(...)` directly — a
plain self-invocation bypasses the Spring AOP proxy, so no transaction would actually open and the split
would compile without doing anything. `persist()` is `protected`, not `public`: CGLIB can advise it, and
it has no reason to be part of the class's public surface.

## Controller shape

`@RestController @RequestMapping("/<domain-singular>") @RequiredArgsConstructor`. The voter field is always named exactly `accessVoter`. Fields are grouped under `// Access voters`, `// Use cases`, and (where applicable) `// Request handlers` comments. **Every handler body is exactly two lines:**

```java
accessVoter.assertCanX();
return xUseCase.execute(...);          // or xRequestHandler.handle(...)
```

Paths are verb-style camelCase (`/getAll`, `/addProduct`, `/getOrdersByPeriod`), not REST-resource style. Follow the existing style rather than "fixing" it piecemeal.

## Voter shape

`@Component`, extends `shared/util/abstraction/voter/AccessVoter`, **one `assertCanX()` per controller handler**, names corresponding 1:1. Today every non-auth voter method is an identical `assertUserIsAuthenticated()` delegate — the per-endpoint granularity is scaffolding for logic that does not exist yet. No voter inspects the resource being accessed, and `AccessVoter.getCurrentUser()` returns `void`, so subclasses cannot do owner or role checks through it.

## Entity shape — Spring Data JDBC, not JPA

`@Table("...")`, `@Id`, `@PersistenceCreator`, `@MappedCollection(idColumn=...)` for child collections, `AggregateReference<T,Integer>` for cross-aggregate FKs.
`@Getter` only, all fields `final`, with `withX()` / `updateFrom()` copy methods for mutation. Child/value entities have no `@Id`.
Soft delete: a nullable `deletedAt` column plus `...AndDeletedAtIsNull` finder suffixes.

Table naming is inconsistent (singular `product`, `brand`, `order`; plural `prices`, `balances`, `storages`, `client_phones`) — match the existing table, do not normalize.

## Repository shape

Interfaces extending `CrudRepository<E,Integer>` with derived query names, or `@Query` (`org.springframework.data.jdbc.repository.query.Query`) with `@Param` for joins. **There are no `*RepositoryImpl` files.** The single hand-written repository is `BalanceRepository`, a `@Repository` class over `JdbcTemplate`.

## DTO style — split by age

Newer domains (`brand`, `storage`, `price`, `product`) use **`record`s** with an entity-copy compact constructor. Older ones (`auth`, `client`, `order`) use Lombok `@Data @NoArgsConstructor @AllArgsConstructor` classes. `LoginRequest` is a record while its sibling `RegisterRequest` is a class.

**New DTOs should be records.** Validation goes on requests via `jakarta.validation` + `@Valid` at the controller. Only `auth` and `order` have `core/request` packages; other domains take query params directly.

## Transactional email

Copy is translated by having **one imported Mailjet template per language**, not by branching inside a
template. `EmailTemplate` holds an id per `Language` and refuses to initialise if one is missing, so a
new language fails at startup rather than at send time. The source HTML lives in `docs/mailjet/` as
`<name>.<lang>.html` — those files are reference copies of what is in the Mailjet console, not loaded
at runtime; keep them structurally identical and change only the copy between them.

The language is a property of the **recipient** (`app_user.language`), never of the request, so an
email triggered by an administrator still reaches the user in their own language. It selects the
template id and is therefore not part of the variables payload.

`MailjetAdapter` is `@Transactional(propagation = Propagation.NEVER)` — see the UseCase shape section.

## Exceptions

Throw `shared/exception/http/{BadRequest,Forbidden,NotFound,Unauthorized}Exception` from use cases and voters, typically via `orElseThrow`. `ControllerAdviceConfig` maps each to a status and a JSON body. Message bracket style is inconsistent (`'%s'` in auth/client, `[%s]` in order/product).

## Schema changes

**Every schema change edits both `src/main/resources/create_db.sql` and `src/test/resources/schema.sql`.** There is no migration tool. They are already out of sync — see `schema.md` Table B before touching either.

## Deviations from the above that already exist

| symbol | expected | actual | why |
|---|---|---|---|
| StorageSearchUseCase | one `execute()`, `@Transactional` | two public methods (`findByErpCode`, `findAll`), neither named execute, no `@Transactional` | serves both HTTP and the ERP importer |
| StorageController | injects an `accessVoter` | injects no voter; no `storage/presentation/voter/` package exists | oversight; still behind `authenticated()` at the filter chain |
| OrderEntity, OrderItemEntity | immutable, `@Getter` + final fields | mutable `@Getter @Setter @NoArgsConstructor` | predates the immutable style; carries TODOs to convert |
| ProductSearchCriteria | lives in `core/request` | lives in `core/usecase` | it is the use case's input record, never bound from HTTP |
| ClientFindByIdUseCase | returns a `*Response` | returns `ClientEntity`, takes an `AggregateReference` | shaped for `AuthLoginUseCase`'s internal call, not for HTTP |
| JwtCreateTokenUseCase | one public method | adds `getTokenValidityInSeconds()` | needed by the login response |
| ProductFindByErpCodeUseCase, BalancesUpdateUseCase | `@Transactional` | none | ERP import path, called inside another transaction |
| ProductLoadUseCase | `@Transactional(rollbackFor=Exception.class)` | bare `@Transactional` | narrower rollback than the rest of the write path |
| ScheduleDataService.exportOrders() | implemented | empty TODO stub | the ERP export direction was never built |
| JwtFilter | `@Component` | plain class, `new JwtFilter(...)` inside `SecurityConfig` | keeps it out of the general servlet filter chain |
