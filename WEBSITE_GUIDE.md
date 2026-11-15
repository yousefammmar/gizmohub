# Website guide — pages and features

| Page | File | Script | Login needed | What it does | API calls | Rules you can trigger from the UI |
|---|---|---|---|---|---|---|
| Home | `index.html` | `home.js` | no | hero, perks, category tiles, 8 featured in-stock products | `GET /api/products`, `/categories` | CART-xx via Add to cart |
| Log in | `login.html` | `login.js` | no | login form, returns to `?next=` page | `POST /api/auth/login` | AUTH-05, AUTH-08 |
| Register | `register.html` | `register.js` | no | account creation (always CUSTOMER) | `POST /api/auth/register` | AUTH-01..04 |
| Products | `products.html` | `products.js` | no | search, category, min/max price; filters kept in the URL | `GET /api/products?...` | PROD-06..08 |
| Product details | `product-details.html?id=N` | `product-details.js` | no (yes to add) | details + free quantity box | `GET /api/products/{id}`, `POST /api/cart/items` | CART-01..04 |
| Cart | `cart.html` | `cart.js` | yes | change quantity, remove, subtotal, shipping hint | `GET /api/cart`, `PUT/DELETE /api/cart/items/{id}`, `GET /api/checkout/summary` | CART-01, 02, 06, 07 |
| Checkout | `checkout.html` | `checkout.js` | yes | address + phone, summary, confirmation | `GET /api/checkout/summary`, `POST /api/orders` | ORDER-02..10 |
| Orders | `orders.html` | `orders.js` | yes | own order history | `GET /api/orders` | ORDER-12 |
| Admin | `admin.html` | `admin.js` | ADMIN | product add/edit/delete, stock, order status | `/api/admin/**` | PROD-01..05, ORDER-11, AUTH-06 |

Shared code: `js/app.js` (API wrapper, header/footer, icons, product card, add-to-cart).
Styling: `css/style.css` (tokens at the top: colours, radius, font).

Notes for testers
- Error messages show the rule ID in brackets, e.g. `Quantity must be between 1 and 10. [CART-01]`.
- Quantity inputs are deliberately **not** limited by HTML `min`/`max`, so invalid values reach the backend and can be tested.
- Useful selectors for automation: `data-testid="product-card"`, `data-product-id`, `data-add-to-cart`, `data-testid="cart-row"`,
  `data-testid="out-of-stock"`, `data-testid="order-card"`, and element ids such as `login-email`, `place-order`, `cart-total`.
- No analytics or tracking is included (QA course project).
