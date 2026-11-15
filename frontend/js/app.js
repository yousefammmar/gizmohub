/* =========================================================
   app.js — shared code loaded by EVERY page (before the page script).
   1. api()        : calls the Java REST API and turns errors into exceptions
   2. helpers      : money formatting, HTML escaping, messages
   3. icons        : inline SVG icons (no icon font / no images needed)
   4. layout       : draws the header + footer and knows who is logged in
   5. shop helpers : product card markup + "Add to cart" used by several pages
   Dependencies: none (plain browser JavaScript). Analytics: none (QA project).
   Safe to edit: texts, icons, header links. Keep api() error handling as is.
   IMPORTANT: business rules live in the Java backend. The frontend only
   displays what the API returns, so every rule can be tested through the API.
   ========================================================= */

/* 1. API ------------------------------------------------------------------ */
async function api(path, options = {}) {
  const init = { method: options.method || 'GET', credentials: 'same-origin', headers: {} };
  if (options.body !== undefined) {
    init.headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(options.body);
  }
  const response = await fetch(path, init);
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    // Backend error shape: {"status":409,"code":"CART-02","message":"..."}
    const error = new Error((data && data.message) || 'Request failed (' + response.status + ').');
    error.status = response.status;
    error.code = data && data.code;
    throw error;
  }
  return data;
}

/* 2. Helpers -------------------------------------------------------------- */
const money = (value) => '$' + Number(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** Escape text before putting it into innerHTML (product names come from admins -> never trust them). */
function esc(text) {
  return String(text == null ? '' : text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

const titleCase = (word) => word.charAt(0) + word.slice(1).toLowerCase();

/** Shows an API error (with its rule ID) or a success message inside an .alert element. */
function showMessage(element, type, message, code) {
  element.className = 'alert alert-' + type;
  element.innerHTML = esc(message) + (code ? '<span class="rule-code">[' + esc(code) + ']</span>' : '');
  element.hidden = false;
}
function hideMessage(element) { element.hidden = true; }

function toast(message) {
  let el = document.getElementById('toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'toast';
    el.className = 'toast';
    el.setAttribute('role', 'status');
    document.body.appendChild(el);
  }
  el.textContent = message;
  el.hidden = false;
  clearTimeout(el._timer);
  el._timer = setTimeout(() => { el.hidden = true; }, 2600);
}

/* 3. Icons (24x24 stroke icons) ------------------------------------------- */
const ICON_PATHS = {
  logo: '<path d="M12 2 3 7v10l9 5 9-5V7z"/><circle cx="12" cy="12" r="3"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>',
  cart: '<circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.6 12.4a1.5 1.5 0 0 0 1.5 1.1h8.8a1.5 1.5 0 0 0 1.5-1.1L21 8H6"/>',
  truck: '<path d="M3 6h11v10H3zM14 10h4l3 3v3h-7z"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/>',
  shield: '<path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6z"/><path d="m9 12 2 2 4-4"/>',
  refresh: '<path d="M21 12a9 9 0 1 1-3-6.7L21 8"/><path d="M21 3v5h-5"/>',
  box: '<path d="M21 8 12 3 3 8v8l9 5 9-5z"/><path d="m3 8 9 5 9-5M12 13v8"/>',
  trash: '<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>',
  LAPTOPS: '<rect x="4" y="5" width="16" height="11" rx="1.5"/><path d="M2 19h20"/>',
  PHONES: '<rect x="7" y="2" width="10" height="20" rx="2"/><path d="M11 18h2"/>',
  AUDIO: '<path d="M4 14v-2a8 8 0 0 1 16 0v2"/><rect x="3" y="14" width="4" height="6" rx="1.5"/><rect x="17" y="14" width="4" height="6" rx="1.5"/>',
  WEARABLES: '<rect x="7" y="7" width="10" height="10" rx="2.5"/><path d="M9 7l1-4h4l1 4M9 17l1 4h4l1-4M12 10v2l1.5 1"/>',
  ACCESSORIES: '<path d="M9 2v5M15 2v5M7 7h10v4a5 5 0 0 1-10 0zM12 16v6"/>',
  GAMING: '<path d="M6 9h12a4 4 0 0 1 4 4v1a3 3 0 0 1-5.4 1.8L15 14H9l-1.6 1.8A3 3 0 0 1 2 14v-1a4 4 0 0 1 4-4z"/><path d="M7 11v3M5.5 12.5h3M16 12h.01M18 13.5h.01"/>'
};
function icon(name, extraClass) {
  return '<svg class="icon ' + (extraClass || '') + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + (ICON_PATHS[name] || ICON_PATHS.box) + '</svg>';
}

/* 4. Layout + session ----------------------------------------------------- */
const GH = { user: null };

/** Asks the backend who is logged in (null when nobody). */
async function loadCurrentUser() {
  try { GH.user = await api('/api/auth/me'); } catch (e) { GH.user = null; }
  return GH.user;
}

async function refreshCartBadge() {
  const badge = document.getElementById('cart-badge');
  if (!badge) return;
  if (!GH.user) { badge.hidden = true; return; }
  try {
    const cart = await api('/api/cart');
    badge.textContent = cart.itemCount;
    badge.hidden = cart.itemCount === 0;
  } catch (e) { badge.hidden = true; }
}

function renderHeader() {
  const page = document.body.dataset.page;
  const link = (href, label, key) => '<a href="' + href + '"' + (page === key ? ' class="active" aria-current="page"' : '') + '>' + label + '</a>';
  const user = GH.user;
  const right = user
    ? '<span class="user-chip" id="user-email" title="' + esc(user.email) + '">' + esc(user.email) + '</span>' +
      '<button class="btn btn-ghost btn-sm" id="logout-button" type="button">Log out</button>'
    : '<a class="btn btn-ghost btn-sm" href="login.html" id="login-link">Log in</a>' +
      '<a class="btn btn-primary btn-sm" href="register.html" id="register-link">Register</a>';

  document.getElementById('site-header').innerHTML =
    '<div class="container header-inner">' +
      '<a class="logo" href="index.html">' + icon('logo') + 'GizmoHub</a>' +
      '<nav class="nav" aria-label="Main">' +
        link('index.html', 'Home', 'home') + link('products.html', 'Products', 'products') +
        (user ? link('orders.html', 'Orders', 'orders') : '') +
        (user && user.role === 'ADMIN' ? link('admin.html', 'Admin', 'admin') : '') +
      '</nav>' +
      '<form class="header-search" action="products.html" role="search">' + icon('search') +
        '<input type="search" name="q" placeholder="Search laptops, phones, audio…" aria-label="Search products">' +
      '</form>' +
      '<div class="header-actions">' +
        '<a class="cart-link" href="cart.html" aria-label="Cart">' + icon('cart') + '<span class="cart-badge" id="cart-badge" hidden>0</span></a>' +
        right +
      '</div>' +
    '</div>';

  const logout = document.getElementById('logout-button');
  if (logout) {
    logout.addEventListener('click', async () => {
      await api('/api/auth/logout', { method: 'POST' });
      window.location.href = 'index.html';
    });
  }
}

function renderFooter() {
  const footer = document.getElementById('site-footer');
  if (!footer) return;
  footer.innerHTML = '<div class="container footer-inner">' +
    '<span>© GizmoHub — Software Quality Assurance course project</span>' +
    '<span>Free shipping on orders of $50 or more · Simulated payments only</span></div>';
}

/** Pages that need a login call this: sends the visitor to login.html and back afterwards. */
function requireLogin() {
  if (!GH.user) {
    window.location.href = 'login.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search);
    return false;
  }
  return true;
}

/** Every page script waits for this before doing its own work: await GH.ready */
GH.ready = (async () => {
  await loadCurrentUser();
  renderHeader();
  renderFooter();
  refreshCartBadge();
})();

/* 5. Shop helpers --------------------------------------------------------- */

/** Product card used on Home and Products pages. PROD-06: stockLabel comes from the backend. */
function productCard(p) {
  const inStock = p.inStock;
  return '<article class="product-card" data-testid="product-card" data-product-id="' + p.id + '">' +
    '<a class="product-media" href="product-details.html?id=' + p.id + '" aria-label="' + esc(p.name) + '">' + icon(p.category) + '</a>' +
    '<div class="product-body">' +
      '<span class="chip">' + esc(titleCase(p.category)) + '</span>' +
      '<a class="product-name" href="product-details.html?id=' + p.id + '">' + esc(p.name) + '</a>' +
      '<span class="price">' + money(p.price) + '</span>' +
      '<div class="product-foot">' +
        (inStock
          ? '<span class="badge badge-in">' + p.stock + ' in stock</span>'
          : '<span class="badge badge-out" data-testid="out-of-stock">' + esc(p.stockLabel) + '</span>') +
        // Button: adds 1 unit -> addToCart() -> toast + cart badge update. Disabled when out of stock.
        '<button class="btn btn-primary btn-block" type="button" data-add-to-cart="' + p.id + '"' + (inStock ? '' : ' disabled') + '>' +
          icon('cart') + (inStock ? 'Add to cart' : 'Out of Stock') + '</button>' +
      '</div>' +
    '</div>' +
  '</article>';
}

/** POST /api/cart/items. Errors (CART-01..04) are shown with their rule ID. */
async function addToCart(productId, quantity) {
  if (!requireLogin()) return false;
  try {
    await api('/api/cart/items', { method: 'POST', body: { productId: productId, quantity: quantity } });
    toast('Added to cart');
    refreshCartBadge();
    return true;
  } catch (e) {
    toast(e.message + (e.code ? ' [' + e.code + ']' : ''));
    return false;
  }
}

/** One click listener for every "Add to cart" button inside a container. */
function wireAddToCartButtons(container) {
  container.addEventListener('click', (event) => {
    const button = event.target.closest('[data-add-to-cart]');
    if (button) addToCart(Number(button.dataset.addToCart), 1);
  });
}
