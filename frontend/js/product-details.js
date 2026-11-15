/* product-details.js — One product with a quantity picker.
   API: GET /api/products/{id}, POST /api/cart/items. Depends on app.js. Analytics: none.
   The quantity input is NOT limited in the browser, so CART-01/CART-02 can be tested from the UI. */
(async function () {
  await GH.ready;
  const box = document.getElementById('product-detail');
  const message = document.getElementById('detail-message');
  const id = new URLSearchParams(location.search).get('id');

  let p;
  try {
    p = await api('/api/products/' + encodeURIComponent(id || ''));
  } catch (e) {
    showMessage(message, 'error', e.message, e.code);
    return;
  }
  document.title = p.name + ' · GizmoHub';

  box.innerHTML =
    '<div class="detail">' +
      '<div class="product-media large">' + icon(p.category) + '</div>' +
      '<div>' +
        '<span class="chip">' + esc(titleCase(p.category)) + '</span>' +
        '<h1>' + esc(p.name) + '</h1>' +
        '<p class="price big">' + money(p.price) + '</p>' +
        '<p>' + esc(p.description) + '</p>' +
        (p.inStock
          ? '<p><span class="badge badge-in">' + p.stock + ' in stock</span></p>'
          : '<p><span class="badge badge-out" data-testid="out-of-stock">' + esc(p.stockLabel) + '</span></p>') +
        '<p class="muted">Up to 10 per order (CART-01).</p>' +
        '<div class="actions-row">' +
          '<label for="detail-qty" class="muted" style="margin:0">Qty</label>' +
          '<input id="detail-qty" class="qty-input" type="number" value="1">' +
          // Button: reads the quantity -> addToCart() -> toast with result or CART-xx error.
          '<button class="btn btn-primary" id="detail-add" type="button"' + (p.inStock ? '' : ' disabled') + '>' +
            icon('cart') + (p.inStock ? 'Add to cart' : 'Out of Stock') + '</button>' +
        '</div>' +
      '</div>' +
    '</div>';

  document.getElementById('detail-add').addEventListener('click', async () => {
    const quantity = Number(document.getElementById('detail-qty').value);
    await addToCart(p.id, Number.isInteger(quantity) ? quantity : null);
  });
})();
