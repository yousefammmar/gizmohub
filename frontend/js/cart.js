/* cart.js — Cart page: change quantity, remove items, see subtotal.
   API: GET /api/cart, PUT/DELETE /api/cart/items/{productId}, GET /api/checkout/summary.
   Every total shown comes from the backend (CART-06/07, ORDER-06/07). Depends on app.js. Analytics: none. */
(async function () {
  await GH.ready;
  if (!requireLogin()) return;
  const itemsBox = document.getElementById('cart-items');
  const summaryBox = document.getElementById('cart-summary');
  const message = document.getElementById('cart-message');

  function drawCart(cart, totals) {
    document.getElementById('cart-count').textContent = cart.itemCount + (cart.itemCount === 1 ? ' item' : ' items');
    if (cart.items.length === 0) {
      itemsBox.innerHTML = '<div class="empty">' + icon('cart') + '<p>Your cart is empty.</p><a class="btn btn-primary" href="products.html">Browse products</a></div>';
      summaryBox.innerHTML = '<h2>Summary</h2><p class="muted">Add a product to see your total.</p>';
      return;
    }
    itemsBox.innerHTML = '<div class="table-wrap"><table><thead><tr><th>Product</th><th class="num">Price</th><th>Qty</th><th class="num">Line total</th><th></th></tr></thead><tbody>' +
      cart.items.map((line) =>
        '<tr data-testid="cart-row" data-product-id="' + line.productId + '">' +
          '<td><div class="cart-product"><span class="thumb">' + icon(line.category) + '</span><div><a href="product-details.html?id=' + line.productId + '">' + esc(line.name) + '</a><div class="muted">' + line.stock + ' in stock</div></div></div></td>' +
          '<td class="num">' + money(line.unitPrice) + '</td>' +
          // Input: changing it calls updateQuantity() -> PUT /api/cart/items/{id} (CART-01/02 errors shown above).
          '<td><input class="qty-input" type="number" value="' + line.quantity + '" data-qty="' + line.productId + '" aria-label="Quantity for ' + esc(line.name) + '"></td>' +
          '<td class="num">' + money(line.lineTotal) + '</td>' +
          // Button: removeItem() -> DELETE /api/cart/items/{id} -> cart redrawn with the new total (CART-07).
          '<td><button class="btn btn-danger btn-sm" type="button" data-remove="' + line.productId + '" aria-label="Remove ' + esc(line.name) + '">' + icon('trash') + '</button></td>' +
        '</tr>'
      ).join('') + '</tbody></table></div>';

    const missing = (50 - Number(totals.subtotal)).toFixed(2);
    summaryBox.innerHTML = '<h2>Summary</h2>' +
      '<div class="summary-row"><span>Subtotal</span><span id="cart-subtotal">' + money(totals.subtotal) + '</span></div>' +
      '<div class="summary-row"><span>Shipping</span><span>' + (Number(totals.shippingFee) === 0 ? 'Free' : money(totals.shippingFee)) + '</span></div>' +
      (Number(totals.shippingFee) > 0 ? '<div class="shipping-note">Add ' + money(missing) + ' more for free shipping.</div>' : '') +
      '<div class="summary-row total"><span>Total</span><span id="cart-total">' + money(totals.total) + '</span></div>' +
      // Button: goes to checkout.html (plain link).
      '<a class="btn btn-primary btn-block" href="checkout.html" id="go-checkout" style="margin-top:12px">Proceed to checkout</a>';
  }

  async function reload() {
    const [cart, totals] = await Promise.all([api('/api/cart'), api('/api/checkout/summary')]);
    drawCart(cart, totals);
    refreshCartBadge();
  }

  async function run(action) {
    hideMessage(message);
    try { await action(); } catch (e) { showMessage(message, 'error', e.message, e.code); }
    await reload();
  }

  itemsBox.addEventListener('change', (event) => {
    const input = event.target.closest('[data-qty]');
    if (!input) return;
    const quantity = Number(input.value);
    run(() => api('/api/cart/items/' + input.dataset.qty, { method: 'PUT', body: { quantity: Number.isInteger(quantity) ? quantity : null } }));
  });
  itemsBox.addEventListener('click', (event) => {
    const button = event.target.closest('[data-remove]');
    if (button) run(() => api('/api/cart/items/' + button.dataset.remove, { method: 'DELETE' }));
  });

  await reload();
})();
