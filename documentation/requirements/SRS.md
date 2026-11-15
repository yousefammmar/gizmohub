# GizmoHub — Software Requirements Specification (SRS)

Version 1.0 · Applies to the code in `backend/` and `frontend/`.
Every requirement ID below is also the `code` the API returns when that rule is broken, e.g.

```json
{ "status": 409, "code": "CART-02", "message": "Only 3 of Orbit Band available." }
```

So a test can check the **status code**, the **rule ID** and the **effect on data**.

---

## 1. Architecture

```
Browser (frontend/*.html + js/*.js)        ← only displays data, no business rules
        │  fetch() JSON, session cookie (JSESSIONID, HttpOnly, SameSite=Lax)
        ▼
Spring Boot (backend/, port 8080) — also serves frontend/ as static files
  configuration/AuthInterceptor   → 401 / 403 before protected controllers run
  controllers/    REST endpoints (thin: read input, call service, return DTO)
  services/       business rules  ← main target for unit, white-box and mutation testing
  validation/     pure input-rule functions ← main target for EQP / BVA
  repositories/   Spring Data JPA
  models/         JPA entities
        ▼
H2 in-memory database (re-created and re-seeded on every start)
```

| Module | Team member | Main classes |
|---|---|---|
| 1 Authentication | Member 1 | `AuthController`, `AuthService`, `AuthValidator`, `AuthInterceptor` |
| 2 Product Management | Member 2 | `ProductController`, `AdminController` (products), `ProductService`, `ProductValidator` |
| 3 Shopping Cart | Member 3 | `CartController`, `CartService`, `OrderCalculator` (subtotal) |
| 4 Checkout & Orders | Member 4 | `OrderController`, `AdminController` (orders), `CheckoutService`, `OrderService`, `OrderCalculator`, `CheckoutValidator` |

### HTTP status conventions
| Status | Meaning |
|---|---|
| 200 / 201 / 204 | success / created / success with no body |
| 400 | input breaks a validation rule |
| 401 | not logged in, or wrong credentials (AUTH-08) |
| 403 | logged in but not allowed (AUTH-06) |
| 404 | resource does not exist **or belongs to another customer** |
| 409 | conflicts with current data (stock, duplicate email, invalid status change) |

---

## 2. Functional requirements

Notation for ranges: `[a, b]` = both ends included, `(a, b]` = `a` excluded, `b` included.

### Module 1 — Authentication

| ID | Description | Inputs | Preconditions | Expected output | Business constraints | Source component |
|---|---|---|---|---|---|---|
| AUTH-01 | Email must have a valid format | `email` | — | Invalid → 400 AUTH-01. Valid → stored trimmed + lower-case | Regex `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`, length ≤ 254, null/blank invalid | `AuthValidator.normalizeEmail` |
| AUTH-02 | Email must be unique | `email` | an account with same email (case-insensitive) exists | 409 AUTH-02, no new account | Compared after normalisation; DB `UNIQUE` constraint as backstop | `AuthService.register`, `User` |
| AUTH-03 | Password length 8–20 | `password` | — | Outside range → 400 AUTH-03 | Length in `[8, 20]` characters, null invalid, any characters allowed | `AuthValidator.validatePassword` |
| AUTH-04 | Confirmation must match | `password`, `confirmPassword` | — | Mismatch → 400 AUTH-04 | Exact, case-sensitive equality | `AuthValidator.validatePasswordConfirmation` |
| AUTH-05 | Login requires valid credentials | `email`, `password` | account exists | Missing field → 400 AUTH-05. Valid → 200 + new session | Email compared case-insensitively; password case-sensitive | `AuthService.login`, `AuthController.login` |
| AUTH-06 | Customers cannot access admin endpoints | session role, request path | logged in as CUSTOMER | Any `/api/admin/**` → 403 AUTH-06 (not logged in → 401) | Enforced on the server before the controller runs | `AuthInterceptor.preHandle` |
| AUTH-07 | Passwords are securely hashed | `password` | — | Only a BCrypt hash is stored; hash never returned by any endpoint | BCrypt, strength 10, random salt | `AppConfig.passwordEncoder`, `AuthService.register` |
| AUTH-08 | Generic login failure message | `email`, `password` | — | Unknown email **and** wrong password both → 401 AUTH-08 "Invalid email or password." | Message must not reveal which field was wrong | `AuthService.login` |

Registration order of checks: AUTH-01 → AUTH-03 → AUTH-04 → AUTH-02 (the first failing rule is reported).
Self-registration always creates role CUSTOMER. Login invalidates any previous session (session-fixation protection); sessions expire after 30 minutes of inactivity; logout (`POST /api/auth/logout`) invalidates the session.

### Module 2 — Product Management

| ID | Description | Inputs | Preconditions | Expected output | Business constraints | Source component |
|---|---|---|---|---|---|---|
| PROD-01 | Name cannot be empty | `name` | admin | null/blank → 400 PROD-01 | Trimmed length in `[1, 100]` | `ProductValidator.validateName` |
| PROD-02 | Price > 0 and ≤ 10,000 | `price` | admin | Outside → 400 PROD-02 | `(0, 10000.00]`, at most 2 decimal places, null invalid, BigDecimal | `ProductValidator.validatePrice` |
| PROD-03 | Stock 0–1,000 | `stock` | admin | Outside → 400 PROD-03 | Integer in `[0, 1000]`, null invalid | `ProductValidator.validateStock` |
| PROD-04 | Category required | `category` | admin | null/blank/unknown → 400 PROD-04 | One of LAPTOPS, PHONES, AUDIO, WEARABLES, ACCESSORIES, GAMING (case-insensitive) | `ProductValidator.parseCategory` |
| PROD-05 | Only admins create/modify products | session | — | Customer → 403, anonymous → 401 | Applies to POST/PUT/PATCH/DELETE under `/api/admin/products` | `AuthInterceptor` |
| PROD-06 | Zero stock shows "Out of Stock" | `stock` | — | `stockLabel = "Out of Stock"`, `inStock = false`, Add-to-cart disabled in UI | stock == 0 → Out of Stock, stock ≥ 1 → In Stock | `ProductService.stockLabel` |
| PROD-07 | Price filter includes products in range | `minPrice`, `maxPrice` | — | Products with `min ≤ price ≤ max` | Both bounds inclusive; either bound may be omitted | `ProductService.matchesFilters` |
| PROD-08 | Invalid search/filter parameters handled consistently | `q`, `category`, `minPrice`, `maxPrice` | — | 400 PROD-08 for: non-numeric price, negative price, `min > max`, unknown category, `q` longer than 100 chars | Blank parameters are ignored (= no filter); `q` matches name or description, case-insensitive | `ProductService.search` + `normalizeQuery`, `parseFilterCategory`, `parseFilterPrice` |

Field order of checks on create/update: PROD-01 → PROD-02 → PROD-03 → PROD-04.
Deleting a product removes it from all carts; existing orders keep their copied name and price.

### Module 3 — Shopping Cart

| ID | Description | Inputs | Preconditions | Expected output | Business constraints | Source component |
|---|---|---|---|---|---|---|
| CART-01 | Quantity 1–10 per product | `quantity` | logged in | Outside → 400 CART-01 | Requested quantity in `[1, 10]`; **after merging** with an existing row the total must also be ≤ 10 | `CartService.validateQuantity`, `addItem`, `updateQuantity` |
| CART-02 | Quantity cannot exceed stock | `quantity`, product stock | product exists, in stock | 409 CART-02 | Total quantity in cart ≤ current stock | `CartService.addItem`, `updateQuantity` |
| CART-03 | Nonexistent product cannot be added | `productId` | logged in | Unknown id → 404 CART-03; missing id → 400 CART-03 | | `CartService.findProduct` |
| CART-04 | Out-of-stock product cannot be added | `productId` | stock == 0 | 409 CART-04 | | `CartService.addItem` |
| CART-05 | Adding existing product updates quantity | `productId`, `quantity` | product already in cart | Same row, quantity = old + new; no duplicate row | DB unique `(user_id, product_id)` | `CartService.addItem`, `CartItem` |
| CART-06 | Subtotal = Σ price × quantity | cart rows | — | `lineTotal = unitPrice × quantity`, `subtotal = Σ lineTotal` | BigDecimal, 2 decimals, HALF_UP | `OrderCalculator.lineTotal`, `subtotal` |
| CART-07 | Removing an item updates the total | `productId` | product in cart | Response cart without the row and with the new subtotal | Removing a product not in the cart → 404 | `CartService.removeItem` |
| CART-08 | A cart belongs only to its customer | session | logged in | Users only ever read/modify their own rows; anonymous → 401 | User id always from the session, never from the request | `CartController.currentUserId`, `CartItemRepository.findByUserIdAndProductId` |

Check order in `addItem`: CART-01 (range) → CART-03 → CART-04 → CART-05 merge → CART-01 (merged total) → CART-02.
`PUT /api/cart/items/{id}` **replaces** the quantity (checks CART-01 then CART-02); `POST` **adds** to it.

### Module 4 — Checkout & Orders

| ID | Description | Inputs | Preconditions | Expected output | Business constraints | Source component |
|---|---|---|---|---|---|---|
| ORDER-01 | Checkout requires authentication | session | — | Anonymous → 401 | | `AuthInterceptor`, `CheckoutService.placeOrder` |
| ORDER-02 | Cart must have ≥ 1 item | cart | logged in | Empty → 400 ORDER-02 | | `CheckoutService.placeOrder` |
| ORDER-03 | Shipping address required | `shippingAddress` | — | 400 ORDER-03 | Trimmed length in `[10, 200]` | `CheckoutValidator.validateAddress` |
| ORDER-04 | Phone number rule | `phone` | — | 400 ORDER-04 | Optional `+` as **first** character only; all other characters digits `0-9`; digit count in `[10, 15]`; surrounding spaces trimmed; null/blank invalid | `CheckoutValidator.validatePhone` |
| ORDER-05 | All quantities available | cart, stock | — | Any line with quantity > stock → 409 ORDER-05 listing every such product | Checked for all lines before anything changes | `CheckoutService.placeOrder` |
| ORDER-06 | Free shipping at ≥ $50 | subtotal | — | shippingFee = 0.00 | `subtotal ≥ 50.00` (inclusive) | `OrderCalculator.shippingFee` |
| ORDER-07 | $5 shipping below $50 | subtotal | — | shippingFee = 5.00 | `0 < subtotal < 50.00` | `OrderCalculator.shippingFee` |
| ORDER-08 | Final total correct | subtotal, fee | — | `total = subtotal + shippingFee` | BigDecimal, 2 decimals | `OrderCalculator.calculate` |
| ORDER-09 | Success reduces inventory, creates order | cart | all checks pass | 201 order (status PLACED); each product stock −= quantity; cart emptied | Order lines copy name + unit price | `CheckoutService.placeOrder` |
| ORDER-10 | Failed checkout changes nothing | — | any check fails | No stock change, no order, cart unchanged | Single `@Transactional` method; checks before writes; `@Version` optimistic locking on `Product` | `CheckoutService.placeOrder` |
| ORDER-11 | Order status transitions (admin) | `status` | admin | Allowed → 200; not allowed → 409 ORDER-11; unknown/blank → 400 ORDER-11 | PLACED→SHIPPED, PLACED→CANCELLED, SHIPPED→DELIVERED only. Cancel adds quantities back to stock | `OrderService.changeStatus`, `isAllowedTransition` |
| ORDER-12 | Customers see only their own orders | order id | logged in | Another customer's order → 404 ORDER-12 | | `OrderService.getOwnOrder`, `history` |

Check order in `placeOrder`: ORDER-01 → ORDER-02 → ORDER-03 → ORDER-04 → ORDER-05 → calculate (06/07/08) → write (09).
Payment is **simulated**: no payment fields exist; the UI states "pay on delivery".

---

## 3. REST API

| Method | Path | Auth | Body | Success | Rules |
|---|---|---|---|---|---|
| POST | /api/auth/register | — | `{email, password, confirmPassword}` | 201 user | AUTH-01..04, 07 |
| POST | /api/auth/login | — | `{email, password}` | 200 user + cookie | AUTH-05, 08 |
| POST | /api/auth/logout | — | — | 204 | |
| GET | /api/auth/me | — | — | 200 user / 401 | |
| GET | /api/products?q=&category=&minPrice=&maxPrice= | — | — | 200 list | PROD-06..08 |
| GET | /api/products/{id} | — | — | 200 / 404 | PROD-06 |
| GET | /api/products/categories | — | — | 200 list | |
| POST | /api/admin/products | ADMIN | `{name, description, category, price, stock}` | 201 | PROD-01..05 |
| PUT | /api/admin/products/{id} | ADMIN | same | 200 / 404 | PROD-01..05 |
| PATCH | /api/admin/products/{id}/stock | ADMIN | `{stock}` | 200 | PROD-03, 05 |
| DELETE | /api/admin/products/{id} | ADMIN | — | 204 / 404 | PROD-05 |
| GET | /api/cart | user | — | 200 cart | CART-06, 08 |
| POST | /api/cart/items | user | `{productId, quantity}` | 200 cart | CART-01..06 |
| PUT | /api/cart/items/{productId} | user | `{quantity}` | 200 cart | CART-01, 02 |
| DELETE | /api/cart/items/{productId} | user | — | 200 cart | CART-07 |
| GET | /api/checkout/summary | user | — | 200 `{itemCount, subtotal, shippingFee, total}` | ORDER-06..08 |
| POST | /api/orders | user | `{shippingAddress, phone}` | 201 order | ORDER-01..10 |
| GET | /api/orders | user | — | 200 own orders | ORDER-12 |
| GET | /api/orders/{id} | user | — | 200 / 404 | ORDER-12 |
| GET | /api/admin/orders | ADMIN | — | 200 all orders | AUTH-06 |
| PATCH | /api/admin/orders/{id}/status | ADMIN | `{status}` | 200 | ORDER-11 |

Other error codes: `INVALID_REQUEST` (malformed JSON / wrong type), `NOT_FOUND`, `METHOD_NOT_ALLOWED`, `PROD-NOT-FOUND`, `CART-NOT-IN-CART`, `CONCURRENT_UPDATE`, `DATA_CONFLICT`, `INTERNAL_ERROR`.

---

## 4. Non-functional requirements (Phase 3)

| ID | Quality aspect | Requirement | Measurable acceptance criterion | How to measure |
|---|---|---|---|---|
| NFR-01 | Performance | Catalog search answers quickly on a developer laptop | After 20 warm-up requests, for 200 requests to `GET /api/products?category=AUDIO` sent 10 in parallel against a freshly started app, **95th percentile ≤ 300 ms** and **0 errors** | `documentation/phase3/perf-check.sh` |
| NFR-02 | Security — authorization | Protected endpoints never answer without the right role | **100 %** of the endpoint × role checks in the Phase 3 security matrix return the expected 401/403/404 | Security test cases ST-xx + `AuthorizationIntegrationTest` |
| NFR-03 | Security — credentials | No password or hash is exposed | 0 API responses contain `password` / `passwordHash`; DB column holds BCrypt (`$2a$10$…`) values only | API inspection + H2 console |

The thresholds are targets set **before** testing. Record the measured values in Phase 3 evidence; do not change the thresholds after seeing results without recording why.
