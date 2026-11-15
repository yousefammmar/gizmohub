# Seed data (deterministic test fixtures)

Created by `backend/src/main/java/com/gizmohub/configuration/DataSeeder.java` on **every start**.
The H2 database is in-memory, so restarting the app (or each JUnit Spring context) always gives exactly this data.
Integration tests run inside `@Transactional` and are rolled back, so no test sees data left by another test.

## Accounts (local test credentials only)

| id | Email | Password | Role |
|---|---|---|---|
| 1 | admin@gizmohub.test | Admin@1234 | ADMIN |
| 2 | customer1@gizmohub.test | Customer@123 | CUSTOMER |
| 3 | customer2@gizmohub.test | Customer@123 | CUSTOMER |
| 4 | customer3@gizmohub.test | Customer@123 | CUSTOMER |

Passwords are stored as BCrypt hashes; the plain values above exist only in `DataSeeder.java` and this document — never in frontend code.

## Products

| id | Name | Category | Price | Stock | Why it is useful for testing |
|---|---|---|---|---|---|
| 1 | NovaBook Air 13 | LAPTOPS | 899.00 | 15 | normal product |
| 2 | NovaBook Pro 16 | LAPTOPS | 2499.00 | 5 | low stock |
| 3 | Titan Workstation X | LAPTOPS | 9999.99 | 2 | price just below max (PROD-02) |
| 4 | Titan Workstation Ultra | LAPTOPS | 10000.00 | 1 | price exactly max; stock 1 |
| 5 | Pulse Phone 12 | PHONES | 699.00 | 25 | normal |
| 6 | Pulse Phone Mini | PHONES | 449.00 | **0** | out of stock (PROD-06, CART-04) |
| 7 | Pulse Phone Lite | PHONES | 299.99 | 8 | normal |
| 8 | EchoBuds Pro | AUDIO | 149.00 | 40 | plenty of stock for quantity tests |
| 9 | EchoBuds Lite | AUDIO | 49.99 | 30 | 1 cent below free shipping (ORDER-07) |
| 10 | BassBox Speaker | AUDIO | 50.00 | 12 | exactly free shipping (ORDER-06) |
| 11 | StudioMax Headphones | AUDIO | 329.00 | **0** | out of stock |
| 12 | Orbit Watch 3 | WEARABLES | 249.00 | 18 | normal |
| 13 | Orbit Band | WEARABLES | 79.99 | 1 | stock 1 (CART-02 boundary) |
| 14 | FitRing Smart Ring | WEARABLES | 199.00 | **0** | out of stock |
| 15 | GizmoHub Sticker | ACCESSORIES | 0.01 | 500 | minimum valid price |
| 16 | USB-C Cable 1m | ACCESSORIES | 0.99 | 1000 | maximum stock (PROD-03) |
| 17 | USB-C Cable 2m | ACCESSORIES | 1.00 | 999 | stock just below max |
| 18 | FastCharge 65W Adapter | ACCESSORIES | 39.99 | 60 | normal |
| 19 | PowerCore 20K Power Bank | ACCESSORIES | 59.00 | 9 | stock 9 < cart max 10 (CART-02 vs CART-01) |
| 20 | Screen Protector Pack | ACCESSORIES | 9.99 | 10 | stock 10 = cart max |
| 21 | Nexus Controller | GAMING | 69.99 | 22 | normal |
| 22 | Nexus Console S | GAMING | 499.00 | 11 | normal |
| 23 | Nexus VR Headset | GAMING | 599.00 | 3 | low stock |
| 24 | Arcade Mouse Pad XL | GAMING | 24.50 | 100 | normal |

24 products, 6 categories, 3 out of stock. There are no seeded orders or cart rows.

**Rule for test writers:** a test that changes data must either run in a rolled-back transaction (JUnit) or be executed on a freshly started app (manual). Write the precondition "fresh app start" in the test case.
