/* orders.js — The logged-in customer's order history (ORDER-12: only own orders).
   API: GET /api/orders. Depends on app.js. Analytics: none. */
(async function () {
  await GH.ready;
  if (!requireLogin()) return;
  const list = document.getElementById('order-list');
  try {
    const orders = await api('/api/orders');
    if (orders.length === 0) {
      list.innerHTML = '<div class="card empty">' + icon('box') + '<p>No orders yet.</p><a class="btn btn-primary" href="products.html">Start shopping</a></div>';
      return;
    }
    list.innerHTML = orders.map((o) =>
      '<article class="card order-card" data-testid="order-card" data-order-id="' + o.id + '">' +
        '<div class="order-head"><h2 style="margin:0">Order #' + o.id + '</h2>' +
          '<span class="status status-' + o.status + '">' + o.status + '</span></div>' +
        '<p class="muted">' + new Date(o.createdAt).toLocaleString() + ' · ' + esc(o.shippingAddress) + '</p>' +
        o.items.map((i) => '<div class="summary-row"><span>' + esc(i.productName) + ' × ' + i.quantity + '</span><span>' + money(i.lineTotal) + '</span></div>').join('') +
        '<div class="summary-row"><span>Shipping</span><span>' + money(o.shippingFee) + '</span></div>' +
        '<div class="summary-row total"><span>Total</span><span>' + money(o.total) + '</span></div>' +
      '</article>'
    ).join('');
  } catch (e) {
    showMessage(document.getElementById('orders-message'), 'error', e.message, e.code);
  }
})();
