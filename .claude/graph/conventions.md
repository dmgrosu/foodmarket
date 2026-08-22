# conventions.md — how to write code in this repo

HAND-WRITTEN. Not regenerated. Update when a pattern genuinely changes.

## Package layout — strict 3-slice per domain

```
domain/<name>/core/{request,response,usecase}    business logic + DTOs
domain/<name>/data                                entities + repositories
domain/<name>/presentation/{controller,voter}     HTTP + authorization
```

`price` has only `core/response` + `data` (value-object domain, no HTTP). `auth` adds a fourth sub-package `presentation/service` for the Spring Security `UserDetailsService` SPI adapter. No `core/adapter` package exists in any domain.

Non-domain code: `config/` (4 classes), `shared/{annotation,dataexchange,enums,exception/http,schedule,util}`.

## Naming

Suffixes are load-bearing: `*Controller`, `*AccessVoter`, `*UseCase`, `*Entity`, `*Repository`, `*Request`, `*Response`, `Erp*Dto`.
Use cases are **noun-first**: `OrderAddProductUseCase`, `ProductFindByErpCodeUseCase` — not `AddProductToOrderUseCase`.

## UseCase shape

`@UseCase` (a `@Component` meta-annotation at `shared/annotation/UseCase.java`) + `@RequiredArgsConstructor`, all fields `final`, **one public `execute(...)` method**.
`@Transactional(readOnly = true)` for reads, `@Transactional(rollbackFor = Exception.class)` for writes.

## Controller shape

`@RestController @RequestMapping("/<domain-singular>") @RequiredArgsConstructor`. The voter field is always named exactly `accessVoter`. Fields are grouped under `// Access voters` and `// Use cases` comments. **Every handler body is exactly two lines:**

```java
accessVoter.assertCanX();
return xUseCase.execute(...);
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
