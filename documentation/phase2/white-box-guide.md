# Phase 2 — White-box testing guide

The application was written so that its Java methods can be analysed by hand: short methods, one rule per
`if`, descriptive variable names, no hidden complexity. This guide tells you **which methods to analyse,
how to measure coverage and mutation score, and where to record results**. It contains **no** coverage
numbers, graphs, path counts or mutation scores — those are your Phase 2 results.

## 1. Tools and commands (run inside `backend/`)

| Purpose | Command | Report |
|---|---|---|
| Run all tests + JaCoCo coverage | `./mvnw test` | `target/site/jacoco/index.html` (also `jacoco.xml`, `jacoco.csv`) |
| Run only unit tests | `./mvnw test -Dtest='com.gizmohub.unit.*Test'` | same |
| Run one test class | `./mvnw test -Dtest=CartServiceTest` | same |
| PIT mutation testing | `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` (+ `mutations.xml`) |

What each tool gives you and what you must add manually:

| Metric required by the course | JaCoCo / PIT gives | You must add manually |
|---|---|---|
| Statement coverage | JaCoCo **line** and **instruction** coverage (green / yellow / red lines) | Map lines to your CFG nodes |
| Decision (branch) coverage | JaCoCo **branch** coverage; yellow diamond = partly covered | Which outcome (T/F) of each decision is missing |
| Condition coverage | JaCoCo counts bytecode branches, which correspond to atomic conditions but it does **not** label them | A table of each atomic condition × T/F actually evaluated |
| Condition/decision, multiple condition (MCC) | — | Truth tables (respecting short-circuit, see §3) |
| Cyclomatic complexity | JaCoCo "Cxty" column per method | V(G) = E − N + 2 from your CFG, or decisions + 1 — compare and explain any difference |
| Basis paths / LCSAJ / data flow | — | Fully manual (templates below) |
| Mutation score | PIT killed / survived / no coverage per mutant | Classify survivors: missing test vs. **equivalent** (with written justification) |

PIT is configured (in `pom.xml`) to mutate `com.gizmohub.services.*` and `com.gizmohub.validation.*` using the
unit tests in `com.gizmohub.unit.*`, with mutator groups DEFAULTS + REMOVE_CONDITIONALS + INLINE_CONSTS. These include
conditional-boundary (off-by-one, e.g. `>=` → `>`), negate-conditionals (relational replacement), math (arithmetic
replacement), increments, inline constant (constant replacement) and remove-conditionals (conditional modification).

## 2. Baseline vs. enhanced test suite

The repository ships a **baseline** test suite (`src/test/java/com/gizmohub/unit` and `.../integration`).
It deliberately does not aim for full coverage, so you have something to improve.

1. Before writing any new test: run `./mvnw test` and PIT, save both HTML reports to
   `documentation/evidence/phase2/before/` (zip the folders), and fill the BEFORE column in §6.
2. Add tests based on your CFG / condition tables / survived mutants.
3. Re-run both tools, save to `documentation/evidence/phase2/after/`, fill the AFTER column.
4. Commit tests and evidence together, and record the commit hash.

## 3. Java short-circuit evaluation — read before doing condition coverage

`a && b` evaluates `b` **only when `a` is true**; `a || b` evaluates `b` **only when `a` is false**.
So in `if (i == 0 && c == '+')` the condition `c == '+'` is never evaluated when `i != 0`.
In your condition tables write `–` (not evaluated) for such cells, and do not claim a condition was
"covered false" if it was never evaluated. Multiple-condition coverage for a short-circuited expression has
fewer feasible rows than 2ⁿ — list which rows are infeasible and why.

## 4. Selected methods (snapshot listings)

Line numbers below are the line numbers in the source file **at the time this guide was generated**. If the code
changes, regenerate the listing (`grep -n "" <file>`) and note the commit hash in your analysis.
Recommended split: every member analyses at least one method from their own module; the
course minimum (two non-trivial functions with conditions, compound Boolean expressions, loops, multiple paths
and variable definitions/uses) is met by `validatePhone` + `addItem` alone.

### CheckoutValidator.validatePhone  (ORDER-04)
File: `backend/src/main/java/com/gizmohub/validation/CheckoutValidator.java` lines 37–58  
Why it is interesting: loop, compound conditions `i == 0 && c == '+'` and `c < '0' \|\| c > '9'`, early exit via `continue` and `throw`, counter variable `digitCount`.

```java
 37      public static String validatePhone(String phone) {
 38          if (phone == null || phone.isBlank()) {
 39              throw BusinessException.badRequest("ORDER-04", "Phone number is required.");
 40          }
 41          String trimmed = phone.trim();
 42          int digitCount = 0;
 43          for (int i = 0; i < trimmed.length(); i++) {
 44              char c = trimmed.charAt(i);
 45              if (i == 0 && c == '+') {
 46                  continue; // leading plus is allowed
 47              }
 48              if (c < '0' || c > '9') {
 49                  throw BusinessException.badRequest("ORDER-04", "Phone number may contain only digits and an optional leading +.");
 50              }
 51              digitCount++;
 52          }
 53          if (digitCount < MIN_PHONE_DIGITS || digitCount > MAX_PHONE_DIGITS) {
 54              throw BusinessException.badRequest("ORDER-04",
 55                      "Phone number must have between " + MIN_PHONE_DIGITS + " and " + MAX_PHONE_DIGITS + " digits.");
 56          }
 57          return trimmed;
 58      }
```

### CartService.addItem  (CART-01..05)
File: `backend/src/main/java/com/gizmohub/services/CartService.java` lines 48–75  
Why it is interesting: sequential decisions, the merge path, two uses of `existing.isPresent()`, variable `newQuantity` defined twice.

```java
 48      public CartResponse addItem(Long userId, Long productId, Integer quantity) {
 49          validateQuantity(quantity);                                                  // CART-01
 50          Product product = findProduct(productId);                                    // CART-03
 51          if (product.getStock() == 0) {                                               // CART-04
 52              throw BusinessException.conflict("CART-04", product.getName() + " is out of stock.");
 53          }
 54  
 55          Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
 56          int newQuantity = quantity;
 57          if (existing.isPresent()) {                                                  // CART-05
 58              newQuantity = existing.get().getQuantity() + quantity;
 59          }
 60          if (newQuantity > MAX_QUANTITY) {                                            // CART-01
 61              throw BusinessException.badRequest("CART-01",
 62                      "You can have at most " + MAX_QUANTITY + " of one product in your cart.");
 63          }
 64          if (newQuantity > product.getStock()) {                                      // CART-02
 65              throw BusinessException.conflict("CART-02",
 66                      "Only " + product.getStock() + " of " + product.getName() + " available.");
 67          }
 68  
 69          if (existing.isPresent()) {
 70              existing.get().setQuantity(newQuantity);
 71          } else {
 72              cartItemRepository.save(new CartItem(userId, product, newQuantity));
 73          }
 74          return getCart(userId);
 75      }
```

### ProductService.matchesFilters  (PROD-07)
File: `backend/src/main/java/com/gizmohub/services/ProductService.java` lines 65–73  
Why it is interesting: pure Boolean logic: 9 atomic conditions in 3 compound expressions — ideal for condition / MC/DC / multiple-condition coverage.

```java
 65      public static boolean matchesFilters(Product product, String text, Category category, BigDecimal min, BigDecimal max) {
 66          boolean textMatches = text == null
 67                  || product.getName().toLowerCase().contains(text)
 68                  || (product.getDescription() != null && product.getDescription().toLowerCase().contains(text));
 69          boolean categoryMatches = category == null || product.getCategory() == category;
 70          boolean priceMatches = (min == null || product.getPrice().compareTo(min) >= 0)
 71                  && (max == null || product.getPrice().compareTo(max) <= 0);
 72          return textMatches && categoryMatches && priceMatches;
 73      }
```

### ProductService.search  (PROD-07, PROD-08)
File: `backend/src/main/java/com/gizmohub/services/ProductService.java` lines 45–62  
Why it is interesting: loop over products, compound `min != null && max != null && min.compareTo(max) > 0`.

```java
 45      public List<ProductResponse> search(String query, String category, String minPrice, String maxPrice) {
 46          String text = normalizeQuery(query);
 47          Category wantedCategory = parseFilterCategory(category);
 48          BigDecimal min = parseFilterPrice(minPrice, "minPrice");
 49          BigDecimal max = parseFilterPrice(maxPrice, "maxPrice");
 50          if (min != null && max != null && min.compareTo(max) > 0) {
 51              throw BusinessException.badRequest("PROD-08", "minPrice cannot be greater than maxPrice.");
 52          }
 53  
 54          // ponytail: filters in Java over all rows; fine for a few hundred products, move to a JPA query if the catalog grows.
 55          List<ProductResponse> results = new ArrayList<>();
 56          for (Product product : productRepository.findAll(Sort.by("id"))) {
 57              if (matchesFilters(product, text, wantedCategory, min, max)) {
 58                  results.add(toResponse(product));
 59              }
 60          }
 61          return results;
 62      }
```

### CheckoutService.placeOrder  (ORDER-01..10)
File: `backend/src/main/java/com/gizmohub/services/CheckoutService.java` lines 49–86  
Why it is interesting: two loops, five decisions, list `unavailable` defined and used in a predicate.

```java
 49      public OrderResponse placeOrder(Long userId, CheckoutRequest request) {
 50          if (userId == null) {                                                              // ORDER-01
 51              throw new BusinessException(HttpStatus.UNAUTHORIZED, "ORDER-01", "Please log in to check out.");
 52          }
 53          List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdAsc(userId);
 54          if (cartItems.isEmpty()) {                                                         // ORDER-02
 55              throw BusinessException.badRequest("ORDER-02", "Your cart is empty.");
 56          }
 57          String address = CheckoutValidator.validateAddress(request.shippingAddress());     // ORDER-03
 58          String phone = CheckoutValidator.validatePhone(request.phone());                   // ORDER-04
 59  
 60          // ORDER-05: first collect every problem, without changing anything.
 61          List<String> unavailable = new ArrayList<>();
 62          for (CartItem item : cartItems) {
 63              Product product = item.getProduct();
 64              if (item.getQuantity() > product.getStock()) {
 65                  unavailable.add(product.getName() + " (requested " + item.getQuantity()
 66                          + ", available " + product.getStock() + ")");
 67              }
 68          }
 69          if (!unavailable.isEmpty()) {
 70              throw BusinessException.conflict("ORDER-05", "Not enough stock for: " + String.join(", ", unavailable));
 71          }
 72  
 73          OrderTotals totals = calculator.calculate(cartItems);                              // ORDER-06/07/08
 74          Order order = new Order(userId, address, phone, totals.subtotal(), totals.shippingFee(), totals.total());
 75  
 76          // ORDER-09: reduce inventory and copy each line into the order.
 77          for (CartItem item : cartItems) {
 78              Product product = item.getProduct();
 79              product.setStock(product.getStock() - item.getQuantity());
 80              order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(),
 81                      item.getQuantity(), calculator.lineTotal(product.getPrice(), item.getQuantity())));
 82          }
 83          Order saved = orderRepository.save(order);
 84          cartItemRepository.deleteAll(cartItems);
 85          return OrderService.toResponse(saved);
 86      }
```

### OrderCalculator.calculate  (ORDER-06..08)
File: `backend/src/main/java/com/gizmohub/services/OrderCalculator.java` lines 45–54  
Why it is interesting: loop accumulation, ternary decision `items.isEmpty() ? ZERO : shippingFee(subtotal)`.

```java
 45      public OrderTotals calculate(List<CartItem> items) {
 46          int itemCount = 0;
 47          for (CartItem item : items) {
 48              itemCount += item.getQuantity();
 49          }
 50          BigDecimal subtotal = subtotal(items);
 51          BigDecimal shipping = items.isEmpty() ? ZERO : shippingFee(subtotal);
 52          BigDecimal total = subtotal.add(shipping);
 53          return new OrderTotals(itemCount, subtotal, shipping, total);
 54      }
```

### OrderCalculator.shippingFee  (ORDER-06/07)
File: `backend/src/main/java/com/gizmohub/services/OrderCalculator.java` lines 37–42  
Why it is interesting: single relational decision — the classic mutation target (>= vs >).

```java
 37      public BigDecimal shippingFee(BigDecimal subtotal) {
 38          if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
 39              return ZERO;
 40          }
 41          return STANDARD_SHIPPING_FEE;
 42      }
```

### OrderService.isAllowedTransition  (ORDER-11)
File: `backend/src/main/java/com/gizmohub/services/OrderService.java` lines 71–74  
Why it is interesting: compound condition with nested OR inside AND.

```java
 71      public static boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
 72          return (from == OrderStatus.PLACED && (to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED))
 73                  || (from == OrderStatus.SHIPPED && to == OrderStatus.DELIVERED);
 74      }
```

## 5. Analysis templates

Copy into `phase2-<method>-<member>.md`. Attach CFG drawings as images in `documentation/evidence/phase2/`.

### 5.1 Control-flow graph
| Node | Source lines | Kind (entry / basic block / decision / exit) | Successors |
|---|---|---|---|
| N1 | | entry | |

Edges E = __ · Nodes N = __ · Decision nodes P = __ · V(G) = E − N + 2 = __ · V(G) = P + 1 = __ · JaCoCo Cxty = __

### 5.2 Statement and decision coverage
| Test case | Inputs | Nodes / lines executed | Decision outcomes covered (D1=T, D2=F, …) |
|---|---|---|---|
| | | | |

Statement coverage = executed statements / total statements = __ / __ = __ %
Decision coverage = covered outcomes / (2 × decisions) = __ / __ = __ %
(Compare with JaCoCo and explain differences, e.g. JaCoCo counts bytecode branches of compound conditions.)

### 5.3 Condition, condition/decision and multiple-condition coverage
| Decision | Atomic conditions | Row | c1 | c2 | c3 | Decision outcome | Feasible? | Test case |
|---|---|---|---|---|---|---|---|---|
| | | 1 | T | – | | | | |

### 5.4 Basis paths
| Path ID | Node sequence | Feasible? | Inputs that force it | Expected result | Test case |
|---|---|---|---|---|---|
| P1 | | | | | |
(Number of basis paths = V(G). Basis-path testing ≠ exhaustive path testing: loops make the total number of paths unbounded.)

### 5.5 LCSAJ
| LCSAJ | Start line (X) | End line (Y) | Jump target (Z) | Test case that exercises it |
|---|---|---|---|---|
| 1 | | | | |
LCSAJ coverage (TER3) = exercised / total = __ / __

### 5.6 Data flow
| Variable | Definitions (line) | c-uses (line) | p-uses (line, edge) |
|---|---|---|---|
| | | | |

| du-pair (var, def, use) | Def-clear path | Covered by test case | Criterion (all-defs / all-c-uses / all-p-uses / all-uses) |
|---|---|---|---|
| | | | |

## 6. Coverage and mutation results — before / after

| Class | Metric | Before (date, commit) | After (date, commit) | Source |
|---|---|---|---|---|
| CheckoutValidator | Line coverage | | | JaCoCo |
| CheckoutValidator | Branch coverage | | | JaCoCo |
| CartService | Line coverage | | | JaCoCo |
| CartService | Branch coverage | | | JaCoCo |
| (all targeted classes) | Mutation score (killed / total) | | | PIT |

### Mutant log (minimum 5 meaningful mutants)
| Mutant ID | Class.method:line | Mutator (relational / arithmetic / constant / conditional / off-by-one) | Original → mutated | PIT status | Killed by test | If survived: missing test or equivalent? Justification |
|---|---|---|---|---|---|---|
| M1 | | | | | | |

A mutant is **equivalent** only if no input can make the mutated program behave differently. Write the argument;
"PIT says survived" is not a proof of equivalence.
