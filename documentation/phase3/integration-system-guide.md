# Phase 3 — Integration, system, non-functional and regression testing

Planned test cases: `../test-cases/integration-test-cases.csv` (IT-xx) and `../test-cases/system-test-cases.csv` (ST-xx).
Nothing in this folder reports results; record executions in the CSVs and store proof in `../evidence/phase3/`.

## 1. Module interactions (integration points)

| Interface | From → To | What crosses the boundary | Where in code | Example cases |
|---|---|---|---|---|
| I1 | Authentication → Cart / Checkout / Orders | session attribute `userId` | `AuthController.login` → `CartController.currentUserId` | IT-04, IT-05 |
| I2 | Authentication → Product admin / Order admin | session attribute `role` | `AuthInterceptor.preHandle` | IT-06, IT-07 |
| I3 | Product Management → Cart | product existence, stock, price | `CartService.addItem` reads `ProductRepository` | IT-08, IT-12, IT-14 |
| I4 | Cart → Checkout | cart rows | `CheckoutService.placeOrder` reads `CartItemRepository` | IT-09, IT-10 |
| I5 | Checkout → Inventory | stock decrement | `CheckoutService.placeOrder` (ORDER-09/10) | IT-09, IT-10 |
| I6 | Checkout → Order history | new order row | `OrderRepository` → `OrderService.history` | IT-09, IT-11 |
| I7 | Order admin → Inventory | stock restore on cancel | `OrderService.changeStatus` | IT-13 |
| I8 | Frontend → Backend | JSON + error `code` | `frontend/js/app.js` `api()` | ST-xx |

Automated integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")` +
`@Transactional` (see `IntegrationTestBase`). Each test runs against the seeded test database and is rolled back.
Example pattern for a new test:

```java
class MyIntegrationTest extends IntegrationTestBase {
    @Test
    void adminLowersStockBelowCartQuantity() throws Exception {
        MockHttpSession customer = login(CUSTOMER1, CUSTOMER_PASSWORD);
        // ... perform requests with .session(customer) and assert status + jsonPath("$.code")
    }
}
```

## 2. End-to-end scenarios

| ID | Scenario | Steps (summary) | Covered by |
|---|---|---|---|
| E2E-01 | Successful purchase | register → login → browse → add → cart → checkout → confirmation → history | ST-01, ST-07 |
| E2E-02 | Invalid login | wrong password, unknown email | ST-02 |
| E2E-03 | Out-of-stock handling | open out-of-stock product; stock drops to 0 between add and checkout | ST-03, BB-ORD-06 |
| E2E-04 | Invalid cart quantity | 0, 11, more than stock | ST-04, ST-05 |
| E2E-05 | Failed checkout then success | invalid phone → fix → order confirmed → visible only to owner | ST-06, ST-08 |

## 3. Non-functional checks

| NFR | Acceptance criterion | Procedure | Evidence to keep |
|---|---|---|---|
| NFR-01 Performance | p95 ≤ 300 ms, 0 errors (200 requests, 10 parallel, after 20 warm-up) | fresh start → `bash documentation/phase3/perf-check.sh` → repeat 3 times | script output, raw file, machine/JDK details |
| NFR-02 Authorization | 100 % of matrix cells give expected status | fill the matrix below with curl / Postman / MockMvc | filled matrix + request/response logs |
| NFR-03 Credential exposure | 0 responses with password fields; only BCrypt hashes in DB | inspect responses of every endpoint; H2 console query | screenshots / logs |

### Security matrix (NFR-02) — expected values
| Endpoint | Anonymous | CUSTOMER | ADMIN |
|---|---|---|---|
| GET /api/products | 200 | 200 | 200 |
| GET /api/cart | 401 | 200 | 200 |
| POST /api/orders | 401 | 201/4xx by rules | 201/4xx by rules |
| GET /api/orders/{other customer's id} | 401 | 404 | 404 |
| POST /api/admin/products | 401 | 403 | 201 |
| DELETE /api/admin/products/{id} | 401 | 403 | 204 |
| GET /api/admin/orders | 401 | 403 | 200 |
| PATCH /api/admin/orders/{id}/status | 401 | 403 | 200/409 |
Add an "Actual" column when you execute it.

## 4. Regression testing procedure

1. A defect is found → write a defect report (`defect-report-template.md`, ID `DEF-xx`).
2. **Before fixing**, write a test that fails because of the defect (or a manual test case marked as regression).
3. Fix the code in a separate commit referencing `DEF-xx`.
4. Re-run: the new test, the full automated suite (`./mvnw test`), PIT (if a service/validation class changed), and the
   manual cases listed for the affected requirement in the RTM.
5. Record everything in `regression-results-template.md` (before-fix and after-fix commit hashes).

A "controlled code change" (e.g. changing the free-shipping threshold in `OrderCalculator`) can be used the same way
if no real defect is available — document it as a planned change, not as a found defect.
