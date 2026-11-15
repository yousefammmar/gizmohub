# Button guide

| Page | Button / control | Handler (file) | Request | Visible result on success | Visible result on error |
|---|---|---|---|---|---|
| all | Log in / Register (header) | link | — | opens page | — |
| all | Log out (header) | click listener in `renderHeader` (`app.js`) | `POST /api/auth/logout` | back to Home, header shows Log in | — |
| all | Cart icon | link | — | opens cart; badge = item count | — |
| all | Search box (header) | form → `products.html?q=` | — | catalog filtered | — |
| Home / Products | Add to cart (card) | `addToCart(id, 1)` (`app.js`) | `POST /api/cart/items` | toast "Added to cart", badge +1 | toast with message + rule ID; not logged in → login page |
| Home | Shop all products / Browse audio | link | — | catalog | — |
| Login | Log in | `handleLogin` (`login.js`) | `POST /api/auth/login` | redirect to `?next=` or Products | red box [AUTH-05] / [AUTH-08] |
| Register | Create account | `handleRegister` (`register.js`) | `POST /api/auth/register` | login page with green "Account created" | red box [AUTH-01..04] |
| Products | Category list item | click listener (`products.js`) | `GET /api/products?category=` | grid + count updated | — |
| Products | Apply filters | `applyFilters` (`products.js`) | `GET /api/products?...` | grid + count updated, URL updated | red box [PROD-08], grid emptied |
| Products | Reset | click listener (`products.js`) | `GET /api/products` | all 24 products | — |
| Product details | Add to cart (with Qty) | click listener → `addToCart(id, qty)` | `POST /api/cart/items` | toast, badge | toast [CART-01..04] |
| Cart | Quantity input (change) | change listener (`cart.js`) | `PUT /api/cart/items/{id}` | totals recalculated | red box [CART-01/02], value reverts |
| Cart | Trash icon | click listener (`cart.js`) | `DELETE /api/cart/items/{id}` | row removed, totals updated (CART-07) | red box |
| Cart | Proceed to checkout | link | — | checkout page | — |
| Checkout | Place order | `placeOrder` (`checkout.js`), disabled while sending | `POST /api/orders` | confirmation with order # and totals | red box [ORDER-02..05], summary refreshed |
| Admin | Products / Orders tabs | click listener (`admin.js`) | — | switches section | — |
| Admin | Save product | submit listener (`admin.js`) | `POST` or `PUT /api/admin/products` | green "Product created/updated", table refreshed | red box [PROD-01..04] |
| Admin | Clear form | `clearForm` (`admin.js`) | — | form back to "Add product" | — |
| Admin | Edit | click listener (`admin.js`) | — | form filled with product | — |
| Admin | Set (stock) | click listener (`admin.js`) | `PATCH /api/admin/products/{id}/stock` | green "Stock updated" | red box [PROD-03] |
| Admin | Trash (delete) | click listener + `confirm()` (`admin.js`) | `DELETE /api/admin/products/{id}` | row removed | red box |
| Admin | Shipped / Cancelled / Delivered | click listener (`admin.js`) | `PATCH /api/admin/orders/{id}/status` | status badge changes; cancel restores stock | red box [ORDER-11] |
