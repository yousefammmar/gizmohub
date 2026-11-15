/* checkout.js — Shipping form, order summary and order confirmation.
   API: GET /api/checkout/summary, POST /api/orders. Depends on app.js. Analytics: none.
   Payment is simulated: no card data exists anywhere in the app. */
(async function () {
  await GH.ready;
  if (!requireLogin()) return;
  const form = document.getElementById('checkout-form');
  const message = document.getElementById('checkout-message');
  const summaryBox = document.getElementById('checkout-summary');

  async function drawSummary() {
    const [cart, totals] = await Promise.all([api('/api/cart'), api('/api/checkout/summary')]);
    summaryBox.innerHTML = '<h2>Order summary</h2>' +
      (cart.items.length === 0 ? '<p class="muted">Your cart is empty. <a href="products.html">Add products</a></p>' : '') +
      cart.items.map((line) =>
        '<div class="summary-row"><span>' + esc(line.name) + ' × ' + line.quantity + '</span><span>' + money(line.lineTotal) + '</span></div>'
      ).join('') +
      '<div class="summary-row" style="border-top:1px solid var(--border);margin-top:8px;padding-top:10px"><span>Subtotal</span><span>' + money(totals.subtotal) + '</span></div>' +
      '<div class="summary-row"><span>Shipping</span><span id="summary-shipping">' + (cart.items.length && Number(totals.shippingFee) === 0 ? 'Free' : money(totals.shippingFee)) + '</span></div>' +
      '<div class="summary-row total"><span>Total</span><span id="summary-total">' + money(totals.total) + '</span></div>';
  }

  form.addEventListener('submit', async function placeOrder(event) {
    event.preventDefault();
    hideMessage(message);
    const button = document.getElementById('place-order');
    button.disabled = true; // prevents double-click creating two orders
    try {
      const order = await api('/api/orders', { method: 'POST', body: {
        shippingAddress: form.shippingAddress.value,
        phone: form.phone.value
      } });
      form.closest('.two-col').hidden = true;
      const box = document.getElementById('order-confirmation');
      box.hidden = false;
      box.innerHTML = '<h2>Thank you! Order #' + order.id + ' is confirmed.</h2>' +
        '<p class="muted">Status: <span class="status status-' + order.status + '">' + order.status + '</span></p>' +
        '<div class="summary-row"><span>Subtotal</span><span>' + money(order.subtotal) + '</span></div>' +
        '<div class="summary-row"><span>Shipping</span><span>' + money(order.shippingFee) + '</span></div>' +
        '<div class="summary-row total"><span>Total</span><span id="confirmation-total">' + money(order.total) + '</span></div>' +
        '<p><a class="btn btn-primary" href="orders.html">View order history</a></p>';
      refreshCartBadge();
    } catch (e) {
      showMessage(message, 'error', e.message, e.code);
      await drawSummary();
    } finally {
      button.disabled = false;
    }
  });

  await drawSummary();
})();
