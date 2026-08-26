# known-issues.md — defects found while indexing

HAND-WRITTEN. Documentation only — **nothing here has been changed**. Recorded so they are not re-discovered every session.
Ordered by severity. Re-verify before acting on any row; these were found by reading, not by running.

## Correctness — will fail in production

| # | where | what |
|---|---|---|
| 1 | `domain/product/data/BalanceRepository.java` | `updateBalances` runs a positional `insert into balances values (?,?,?)`. Prod's `balances` has an `id` column first, so the three binds land in `id,storage_id,product_id` and `quantity NOT NULL` is violated on every row. Test schema has exactly 3 columns so the SQL is correct there, and `BalancesUpdateUseCaseTest` mocks the repository — the nightly balance import is broken in prod with zero test signal. |
| 2 | `domain/order/data/OrderEntity.java`, `OrderItemEntity.java` | Both declare a non-transient `UUID uuid` field with **no `uuid` column in either SQL file**. Any write to `order`/`order_product` should fail on an unknown column. No test exercises those tables, so this is entirely uncovered. |
| 3 | `src/main/resources/create_db.sql` | `app_user.email` has no unique index in prod (the test schema does). `AppUserRepository.findByEmail` returns `Optional` and will throw `IncorrectResultSizeDataAccessException` on the first duplicate registration. |
| 4 | `create_db.sql` vs `src/test/resources/schema.sql` | Was 49 drifted columns; the auth/client blockers are now fixed — `app_user.state`/`deleted_at`, `app_user_role` (was `app_user_roles`), and `client.created_at`/`email` are all in the test schema, unblocking `AuthRegistrationFlowTest` and `ClientRepositoryTest`. Roughly 40 columns still drift, mostly `timestamptz`-vs-`TIMESTAMP` and missing constraints/indexes — full table in `schema.md`. |
| 5 | `shared/dataexchange/core/usecase/ImportProductsUseCase.java` | `toBrands` passes `BrandEntity(name, erpCode)` in swapped argument order. |
| 6 | `shared/dataexchange/dto/ErpAddressDto.java` | Maps XML attribute `desrc`; fixtures write `descr`. `ImportClientsUseCaseTest` asserts `description` is `null` — the test encodes the bug rather than catching it. |
| 7 | `shared/dataexchange/dto/ErpPhoneDto.java` | Declares `@XmlRootElement(name = "client")`. Harmless only because it is always a nested element. |
| 8 | `domain/auth/core/usecase/JwtVerifyTokenUseCase.java` | Returns `null` on verification failure rather than throwing; the caller must remember to null-check. |

## Security

| # | where | what |
|---|---|---|
| 9 | `src/main/resources/application.yml` | `jwt.token.secret` is a hard-coded literal committed to the repo. Rotating it requires a code change and invalidates all issued tokens. |
| 10 | `src/main/resources/application-local.yml` | Committed plaintext dev DB password, plus `DATA_FOLDER_PATH` pointing at one developer's home directory (`/home/dgrosu/Downloads/`). |
| 11 | `config/SecurityConfig.java` | `setAllowedOriginPatterns("*")` together with `setAllowCredentials(true)` reflects **any** origin with credentials. There is an in-code WARNING comment acknowledging it. |
| 12 | `domain/storage/presentation/controller/StorageController.java` | The only controller with no `AccessVoter`. Still behind `authenticated()` at the filter chain, so this is a defense-in-depth gap, not an open endpoint. |
| 13 | across all voters | *(Partly resolved: `JwtGetAuthenticationUseCase` now rejects any non-ACTIVE user on every request, so suspension takes effect immediately instead of at token expiry.)* `Role.ADMIN`/`Role.USER` are assigned at registration and materialized as authorities, but **never checked** — no `hasRole`, no `@PreAuthorize`, no role rule in the filter chain. No voter inspects the resource being accessed either, so any authenticated user can read or mutate any other client's order by id. |

## Performance

| # | where | what |
|---|---|---|
| 14 | `OrderAddProductUseCase`, `OrderFindByIdUseCase`, `OrderSearchByPeriodUseCase` | `productRepository.findNameById(...)` is called once per order item inside a stream map. In `OrderSearchByPeriodUseCase` that is per item per order across a whole page. |
| 15 | `ProductGroupSearchUseCase.getGroupsHierarchy` | Re-runs `findAllNonEmpty(storageId)` at every level of the recursion. |
| 16 | `ProductSearchUseCase.addAllParentsToMap` | One `findById` per ancestor per product. |

## Test integrity

| # | where | what |
|---|---|---|
| 17 | the three `Import*UseCaseTest` classes | Each **writes its own XML fixture** in `@BeforeEach` into `src/test/resources/dataExchange/`, a directory that is not committed. Since the use case deletes the file on success, fixtures regenerate every run. This couples the tests to a relative CWD and makes them order- and state-sensitive. |
| 18 | `src/test/resources/application.yml` | Scheduling is enabled app-wide, so `@SpringBootTest` runs fire the ERP importer against that folder every 3 seconds. |
| 19 | `domain/storage/data/StorageRepositoryTest.java` | Cleans up with `delete()` in the test body instead of `@Transactional` rollback; a mid-test failure leaves rows behind. |
| 20 | `.github/workflows/mavenTest.yml` | `actions/checkout@v2` and `actions/setup-java@v1`; the latter has no `distribution:` input, which modern JDK setup requires. Runs `mvn test` only — no `verify`, no frontend build, no coverage. |

## Frontend

| # | where | what |
|---|---|---|
| 21 | `store/actions/cartActions.js` | `addProductToCart` posts only `{orderId, productId, quantity}`, but `AddProductToOrderRequest` also declares `clientId`, `storageId`, and `priceType` with validation. |
| 22 | `components/orders/Cart.js` | Passes a **productId** into the `{itemId}` slot of `DELETE /order/deleteProduct/{orderId}/{itemId}`, while the reducer filters by `product.productId`. |
| 23 | `store/actions/cartActions.js` | `PLACE_ORDER_FAIL` is declared and handled in the reducer but never dispatched — the `placeOrder` catch only toasts, leaving `isPlacing` stuck `true`. |
| 24 | `axios-instance.js` | No response interceptor, so an expired token produces a silent 401 with no auto-logout. Expiry is a client-side `setTimeout` only. |
| 25 | `App.js` | `authCheckState()` is called during render rather than in an effect. |
| 26 | `components/home/Stats.js` | Dead code — never imported. `App.test.js` is the stale CRA smoke test asserting "learn react" and will fail against the current `App`. |
| 27 | `frontend/README.md` | Tells users to see `.env.example`, which does not exist. `npm run build:prod` hard-codes the production API host as an IP literal. |
