# Phase 1 — Black-box test basis

This document lists the **facts about the application** that the four team members need for
Equivalence Partitioning, Boundary Value Analysis, Cause–Effect Graphing and Decision Tables.
It does **not** contain your analysis: each member builds their own partitions, graphs and tables
(use `phase1-analysis-template.md`) and their own test cases (`../test-cases/black-box-test-cases.csv`).
All values come from `../requirements/SRS.md` and the Java code it references.

---

## Module 1 — Authentication (Member 1)

### Input domains and constraints
| Input | Type | Valid domain | Boundaries | Inclusive? | Invalid examples to consider |
|---|---|---|---|---|---|
| email | string | matches `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` after trim + lower-case, length ≤ 254 | length 254 / 255; TLD length 1 / 2 | ≤ 254 inclusive; TLD ≥ 2 inclusive | null, "", "   ", no `@`, no TLD, space inside |
| password | string | length 8..20 | 7, 8, 9, 19, 20, 21 | both inclusive | null, "" |
| confirmPassword | string | equal to password (case-sensitive) | — | — | null, different case |
| email (login) | string | any; must belong to an account | — | — | unknown email |
| role (session) | CUSTOMER / ADMIN / none | — | — | — | — |

### Causes and effects available for cause–effect graphing
**Registration** (`AuthService.register`, checks in this order, first failure wins)
- C1 email format valid · C2 password length in range · C3 confirmation equals password · C4 email not already used
- E1 201 account created · E2 400 AUTH-01 · E3 400 AUTH-03 · E4 400 AUTH-04 · E5 409 AUTH-02
- Relations: E1 = C1 ∧ C2 ∧ C3 ∧ C4. Because of the check order, E3 requires C1; E4 requires C1 ∧ C2; E5 requires C1 ∧ C2 ∧ C3. Only one effect occurs per request (exclusive constraint on effects).

**Login** (`AuthService.login`)
- C1 email given and password given · C2 account with this email exists · C3 password matches hash
- E1 200 + session · E2 400 AUTH-05 · E3 401 AUTH-08 (same message for ¬C2 and ¬C3)
- Note: if ¬C2 then C3 cannot be evaluated (infeasible combination "¬C2 ∧ C3").

**Access control** (`AuthInterceptor.preHandle`, applies to `/api/admin/**`, `/api/cart/**`, `/api/orders/**`, `/api/checkout/**`)
- C1 logged in · C2 path starts with `/api/admin/` · C3 role is ADMIN
- E1 continue · E2 401 · E3 403 AUTH-06
- E2 = ¬C1; E3 = C1 ∧ C2 ∧ ¬C3; E1 = C1 ∧ (¬C2 ∨ C3). Combinations "¬C1 ∧ C3" are infeasible (no role without login).

---

## Module 2 — Product Management (Member 2)

### Input domains and constraints
| Input | Type | Valid domain | Boundaries | Inclusive? |
|---|---|---|---|---|
| name | string | trimmed length 1..100 | 0, 1, 100, 101 | both inclusive |
| price | decimal | (0, 10000.00], max 2 decimals | 0.00, 0.01, 9999.99, 10000.00, 10000.01 | min **exclusive**, max inclusive |
| stock | integer | 0..1000 | -1, 0, 1, 999, 1000, 1001 | both inclusive |
| category | string | one of 6 enum values, case-insensitive | — | — |
| minPrice / maxPrice (filter) | string → decimal | blank (= no filter) or decimal ≥ 0; min ≤ max | -0.01, 0, equal min=max, min > max | ≥ 0 inclusive; product included when min ≤ price ≤ max |
| q (filter) | string | blank or ≤ 100 chars after trim | 100, 101 | inclusive |
| category (filter) | string | blank or enum value | — | — |

Multidimensional partitioning candidates: (price class × stock class × category class) for create/update; (minPrice class × maxPrice class) for the filter — including the cross-field rule min ≤ max.

### Causes and effects
**Create/update product** (checks in order PROD-01 → 02 → 03 → 04, then authorization is already done by the interceptor)
- C1 admin · C2 name valid · C3 price valid · C4 stock valid · C5 category valid
- E1 201/200 saved · E2 401/403 · E3..E6 400 PROD-01/02/03/04

**Search filter** (`ProductService.search` and `matchesFilters`)
- C1 minPrice blank · C2 minPrice numeric · C3 minPrice ≥ 0 · C4 same three for maxPrice · C5 min ≤ max · C6 category blank or known · C7 q ≤ 100 chars
- Per product: D1 text matches (q null OR name contains OR description contains) · D2 category matches · D3 price ≥ min (or no min) · D4 price ≤ max (or no max)
- Product returned = D1 ∧ D2 ∧ D3 ∧ D4.

**Stock label**: stock = 0 → "Out of Stock", stock ≥ 1 → "In Stock" (`ProductService.stockLabel`).

---

## Module 3 — Shopping Cart (Member 3)

### Input domains and constraints
| Input | Type | Valid domain | Boundaries | Inclusive? |
|---|---|---|---|---|
| quantity (requested) | integer | 1..10 | 0, 1, 2, 9, 10, 11 | both inclusive |
| quantity after merge (existing + requested) | integer | ≤ 10 | 10, 11 | inclusive |
| quantity vs stock | integer | quantity ≤ stock | stock-1, stock, stock+1 | inclusive |
| productId | long | id of an existing product | missing, unknown (e.g. 9999) | — |
| product stock | integer | ≥ 1 to be addable | 0, 1 | — |

Useful seed products: stock 0 (ids 6, 11, 14), stock 1 (id 13), stock 9 (id 19), stock 10 (id 20), stock 1000 (id 16).

### Causes and effects — `CartService.addItem` (check order)
- C1 quantity in 1..10 · C2 product exists · C3 stock > 0 · C4 product already in cart · C5 merged quantity ≤ 10 · C6 merged quantity ≤ stock
- E1 200 new row · E2 200 existing row updated · E3 400 CART-01 · E4 404/400 CART-03 · E5 409 CART-04 · E6 409 CART-02
- E1 = C1 ∧ C2 ∧ C3 ∧ ¬C4 ∧ C5 ∧ C6; E2 = C1 ∧ C2 ∧ C3 ∧ C4 ∧ C5 ∧ C6.
- Constraint: when ¬C4, merged quantity = requested quantity, so C1 implies C5 (combination "C1 ∧ ¬C4 ∧ ¬C5" is infeasible).

**Calculations**: lineTotal = unitPrice × quantity (2 decimals, HALF_UP); subtotal = Σ lineTotal; itemCount = Σ quantity.

---

## Module 4 — Checkout & Orders (Member 4)

### Input domains and constraints
| Input | Type | Valid domain | Boundaries | Inclusive? |
|---|---|---|---|---|
| shippingAddress | string | trimmed length 10..200 | 9, 10, 200, 201 | both inclusive |
| phone | string | optional `+` at position 0 only; other chars digits; digit count 10..15 | 9, 10, 15, 16 digits; `+` with 10 digits | both inclusive |
| cart size | rows | ≥ 1 | 0, 1 | — |
| subtotal | decimal | > 0 | 49.99, 50.00, 50.01 | free shipping when ≥ 50.00 (inclusive) |
| order status change | enum | see transition table | — | — |

### Causes and effects — `CheckoutService.placeOrder` (check order)
- C1 logged in · C2 cart not empty · C3 address valid · C4 phone valid · C5 every line quantity ≤ stock · C6 subtotal ≥ 50
- E1 201 order (fee 0) · E2 201 order (fee 5) · E3 401 · E4 400 ORDER-02 · E5 400 ORDER-03 · E6 400 ORDER-04 · E7 409 ORDER-05
- E1 = C1 ∧ C2 ∧ C3 ∧ C4 ∧ C5 ∧ C6; E2 = C1 ∧ C2 ∧ C3 ∧ C4 ∧ C5 ∧ ¬C6.
- Every error effect implies: no stock change, no order row, cart unchanged (ORDER-10).

### Order status transitions (ORDER-11)
| from \ to | PLACED | SHIPPED | DELIVERED | CANCELLED |
|---|---|---|---|---|
| PLACED | ✗ | ✓ | ✗ | ✓ (stock restored) |
| SHIPPED | ✗ | ✗ | ✓ | ✗ |
| DELIVERED | ✗ | ✗ | ✗ | ✗ |
| CANCELLED | ✗ | ✗ | ✗ | ✗ |
✗ → 409 ORDER-11. Unknown / blank status → 400 ORDER-11. Unknown order id → 404.
