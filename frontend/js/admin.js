/* admin.js — Product CRUD, stock updates and order status for administrators.
   API: /api/admin/** (the backend returns 403 AUTH-06 for customers — hiding the link is NOT the protection).
   Depends on app.js. Analytics: none. */
(async function () {
  await GH.ready;
  if (!requireLogin()) return;
  const message = document.getElementById('admin-message');
  if (GH.user.role !== 'ADMIN') {
    showMessage(message, 'error', 'Administrator access required.', 'AUTH-06');
    document.querySelectorAll('.tabs, #tab-products, #tab-orders').forEach((el) => { el.hidden = true; });
    return;
  }

  const form = document.getElementById('product-form');
  const fields = {
    id: document.getElementById('product-id'),
    name: document.getElementById('product-name'),
    description: document.getElementById('product-description'),
    category: document.getElementById('product-category'),
    price: document.getElementById('product-price'),
    stock: document.getElementById('product-stock')
  };
  const categories = await api('/api/products/categories');
  fields.category.innerHTML = '<option value="">Choose…</option>' + categories.map((c) => '<option value="' + c + '">' + titleCase(c) + '</option>').join('');

  let products = [];

  async function run(action, successText) {
    hideMessage(message);
    try {
      await action();
      if (successText) showMessage(message, 'success', successText);
    } catch (e) {
      showMessage(message, 'error', e.message, e.code);
    }
  }

  // ---- Products ----
  async function loadProducts() {
    products = await api('/api/products');
    document.getElementById('admin-products').innerHTML =
      '<table><thead><tr><th>ID</th><th>Name</th><th>Category</th><th class="num">Price</th><th>Stock</th><th></th></tr></thead><tbody>' +
      products.map((p) =>
        '<tr data-product-id="' + p.id + '">' +
          '<td>' + p.id + '</td><td>' + esc(p.name) + (p.inStock ? '' : ' <span class="badge badge-out">' + esc(p.stockLabel) + '</span>') + '</td>' +
          '<td>' + titleCase(p.category) + '</td><td class="num">' + money(p.price) + '</td>' +
          // Input + button: updateStock() -> PATCH /api/admin/products/{id}/stock (PROD-03).
          '<td><div class="actions-row"><input class="qty-input" value="' + p.stock + '" data-stock-input="' + p.id + '" aria-label="Stock for ' + esc(p.name) + '">' +
            '<button class="btn btn-outline btn-sm" type="button" data-stock="' + p.id + '">Set</button></div></td>' +
          // Buttons: Edit fills the form; Delete -> DELETE /api/admin/products/{id} after confirm().
          '<td><div class="actions-row"><button class="btn btn-outline btn-sm" type="button" data-edit="' + p.id + '">Edit</button>' +
            '<button class="btn btn-danger btn-sm" type="button" data-delete="' + p.id + '" aria-label="Delete ' + esc(p.name) + '">' + icon('trash') + '</button></div></td>' +
        '</tr>'
      ).join('') + '</tbody></table>';
  }

  function clearForm() {
    form.reset();
    fields.id.value = '';
    document.getElementById('product-form-title').textContent = 'Add product';
  }

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const body = {
      name: fields.name.value,
      description: fields.description.value,
      category: fields.category.value,
      price: fields.price.value.trim() === '' ? null : fields.price.value.trim(),
      stock: fields.stock.value.trim() === '' ? null : fields.stock.value.trim()
    };
    const id = fields.id.value;
    run(async () => {
      await api(id ? '/api/admin/products/' + id : '/api/admin/products', { method: id ? 'PUT' : 'POST', body: body });
      clearForm();
      await loadProducts();
    }, id ? 'Product updated.' : 'Product created.');
  });
  document.getElementById('product-cancel').addEventListener('click', clearForm);

  document.getElementById('admin-products').addEventListener('click', (event) => {
    const edit = event.target.closest('[data-edit]');
    const del = event.target.closest('[data-delete]');
    const stock = event.target.closest('[data-stock]');
    if (edit) {
      const p = products.find((x) => x.id === Number(edit.dataset.edit));
      fields.id.value = p.id;
      fields.name.value = p.name;
      fields.description.value = p.description || '';
      fields.category.value = p.category;
      fields.price.value = p.price;
      fields.stock.value = p.stock;
      document.getElementById('product-form-title').textContent = 'Edit product #' + p.id;
      fields.name.focus();
    } else if (del) {
      if (!confirm('Delete this product? It is also removed from every cart.')) return;
      run(async () => { await api('/api/admin/products/' + del.dataset.delete, { method: 'DELETE' }); await loadProducts(); }, 'Product deleted.');
    } else if (stock) {
      const value = document.querySelector('[data-stock-input="' + stock.dataset.stock + '"]').value.trim();
      run(async () => {
        await api('/api/admin/products/' + stock.dataset.stock + '/stock', { method: 'PATCH', body: { stock: value === '' ? null : value } });
        await loadProducts();
      }, 'Stock updated.');
    }
  });

  // ---- Orders ----
  const NEXT = { PLACED: ['SHIPPED', 'CANCELLED'], SHIPPED: ['DELIVERED'], DELIVERED: [], CANCELLED: [] }; // display only; ORDER-11 is enforced in Java
  async function loadOrders() {
    const orders = await api('/api/admin/orders');
    document.getElementById('admin-orders').innerHTML = orders.length === 0 ? '<p class="muted">No orders yet.</p>' :
      '<table><thead><tr><th>Order</th><th>Customer</th><th>Date</th><th class="num">Total</th><th>Status</th><th>Change</th></tr></thead><tbody>' +
      orders.map((o) =>
        '<tr><td>#' + o.id + '</td><td>User ' + o.userId + '</td><td>' + new Date(o.createdAt).toLocaleString() + '</td>' +
        '<td class="num">' + money(o.total) + '</td><td><span class="status status-' + o.status + '">' + o.status + '</span></td>' +
        // Buttons: changeStatus() -> PATCH /api/admin/orders/{id}/status (ORDER-11). Cancel puts stock back.
        '<td><div class="actions-row">' + NEXT[o.status].map((s) =>
          '<button class="btn btn-outline btn-sm" type="button" data-order="' + o.id + '" data-status="' + s + '">' + titleCase(s) + '</button>').join('') +
        '</div></td></tr>'
      ).join('') + '</tbody></table>';
  }
  document.getElementById('admin-orders').addEventListener('click', (event) => {
    const button = event.target.closest('[data-status]');
    if (!button) return;
    run(async () => {
      await api('/api/admin/orders/' + button.dataset.order + '/status', { method: 'PATCH', body: { status: button.dataset.status } });
      await Promise.all([loadOrders(), loadProducts()]);
    }, 'Order status updated.');
  });

  // ---- Tabs ----
  document.querySelector('.tabs').addEventListener('click', (event) => {
    const tab = event.target.closest('[data-tab]');
    if (!tab) return;
    document.querySelectorAll('.tabs button').forEach((b) => b.classList.toggle('active', b === tab));
    document.getElementById('tab-products').hidden = tab.dataset.tab !== 'products';
    document.getElementById('tab-orders').hidden = tab.dataset.tab !== 'orders';
  });

  await Promise.all([loadProducts(), loadOrders()]);
})();
