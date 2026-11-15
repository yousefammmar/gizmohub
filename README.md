# GizmoHub — QA-ready e-commerce application

A small but complete electronics store built for a **Software Quality Assurance and Testing** course.
Four modules (one per team member), real business rules with IDs, deterministic seed data, JUnit 5 / Mockito /
MockMvc tests, JaCoCo coverage and PIT mutation testing — plus requirement, traceability and test documentation templates.

> Implementation and QA evaluation are separate. The repository contains a **baseline** test suite and **planned**
> test cases. No test results, coverage percentages or mutation scores are recorded in the documentation — the team
> produces those by executing the tests.

## 1. Run it

Requirements: **JDK 17 or newer** (`java -version`; the suite was run on JDK 17 and 25). Maven is not needed — the wrapper downloads it.

```bash
cd backend
./mvnw spring-boot:run
```

Open <http://localhost:8080>. The backend also serves the `frontend/` folder, so there is nothing else to start.
The database is in-memory H2: **every restart gives the same fresh seed data**.
H2 console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:gizmohub`, user `sa`, empty password).

Windows: use `mvnw.cmd` instead of `./mvnw`.

### Test accounts (local only)
| Email | Password | Role |
|---|---|---|
| admin@gizmohub.test | Admin@1234 | ADMIN |
| customer1@gizmohub.test | Customer@123 | CUSTOMER |
| customer2@gizmohub.test | Customer@123 | CUSTOMER |
| customer3@gizmohub.test | Customer@123 | CUSTOMER |

## 2. Test it

```bash
cd backend
./mvnw test                                                   # JUnit 5 + JaCoCo → target/site/jacoco/index.html
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage  # PIT → target/pit-reports/index.html
```

## 3. Project structure

```
gizmohub/
├── frontend/                 HTML pages + css/style.css + js/ (app.js shared, one script per page)
├── backend/
│   ├── pom.xml               Spring Boot 3.5, H2, JaCoCo, PIT
│   └── src/
│       ├── main/java/com/gizmohub/
│       │   ├── controllers/  REST endpoints (thin)
│       │   ├── services/     business rules  ← unit / white-box / mutation targets
│       │   ├── validation/   pure input rules ← EQP / BVA targets
│       │   ├── repositories/ Spring Data JPA
│       │   ├── models/       JPA entities
│       │   ├── dto/          request / response records
│       │   └── configuration/ auth interceptor, error handler, seed data
│       ├── main/resources/application.properties
│       └── test/java/com/gizmohub/
│           ├── unit/         fast tests, no Spring context (Mockito)
│           └── integration/  @SpringBootTest + MockMvc, rolled back after each test
└── documentation/
    ├── requirements/  SRS.md, RTM.csv, seed-data.md
    ├── phase1/        black-box test basis, analysis template, Test Plan v1.0
    ├── phase2/        white-box guide (selected methods), Test Plan v2.0
    ├── phase3/        integration/system guide, perf script, defect/regression/final templates, Test Plan v3.0
    ├── test-cases/    black-box, white-box, integration, system test cases (CSV → open in Excel)
    └── evidence/      where executed-test proof goes
```

## 4. Modules and rules

| Module | Member | Rules | Main classes |
|---|---|---|---|
| Authentication | 1 | AUTH-01..08 | `AuthService`, `AuthValidator`, `AuthInterceptor` |
| Product Management | 2 | PROD-01..08 | `ProductService`, `ProductValidator`, `AdminController` |
| Shopping Cart | 3 | CART-01..08 | `CartService`, `OrderCalculator` |
| Checkout & Orders | 4 | ORDER-01..12 | `CheckoutService`, `OrderService`, `OrderCalculator`, `CheckoutValidator` |

Full definitions with inputs, preconditions, boundaries and source components: `documentation/requirements/SRS.md`.
Every API error returns the rule ID: `{"status":409,"code":"CART-02","message":"..."}`.

## 5. Design decisions (and their limits)

- **Session auth without Spring Security filters.** BCrypt comes from `spring-security-crypto`; access control is one
  readable `HandlerInterceptor`. No CSRF tokens: the session cookie is `SameSite=Lax` and every write endpoint takes JSON,
  which is enough for a local course project but not for a public deployment.
- **Search filtering in Java** (`ProductService.matchesFilters`) instead of SQL, so it can be unit tested and analysed.
  Fine for hundreds of products; move it to a JPA query for a large catalog.
- **Optimistic locking** (`@Version` on `Product`) protects stock against two simultaneous checkouts; the loser gets
  409 `CONCURRENT_UPDATE` and nothing is changed.
- **Simulated payment only.** There are no payment fields at all.
- **Plain HTML/CSS/JS.** No framework, no build step; business rules are only in Java, so the UI can be used to
  trigger every backend error.

## 6. Implementation plan (as built)

| Step | Content | Verified by |
|---|---|---|
| 1 | Maven project, H2, seed data | app starts, `GET /api/products` returns 24 products |
| 2 | Authentication | register/login/logout through API and UI |
| 3 | Product management | catalog, filters, admin CRUD |
| 4 | Shopping cart | add/merge/update/remove, totals |
| 5 | Checkout & orders | order placement, stock decrement, history, status |
| 6 | Frontend integration | all 9 pages exercised in a browser (desktop + 375 px mobile) |
| 7 | Requirements | `documentation/requirements/` |
| 8 | JUnit infrastructure + baseline tests | `./mvnw test` |
| 9 | JaCoCo + PIT | reports generated |
| 10 | QA templates + requirement-to-code map | `documentation/` |

More: `WEBSITE_GUIDE.md` (pages and features) · `BUTTON_GUIDE.md` (every button and what it calls).
