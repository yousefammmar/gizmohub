/* products.js — Catalog with search, category and price filters.
   API: GET /api/products?q=&category=&minPrice=&maxPrice=  (filtering happens in Java: PROD-07/08)
   The filters are kept in the URL so a test case can be reproduced by opening the same link.
   Depends on app.js. Analytics: none. */
(async function () {
  await GH.ready;
  const form = document.getElementById('filter-form');
  const grid = document.getElementById('product-grid');
  const message = document.getElementById('catalog-message');
  const categoryList = document.getElementById('category-list');
  let selectedCategory = '';

  const categories = await api('/api/products/categories');

  function drawCategories() {
    const options = [['', 'All categories']].concat(categories.map((c) => [c, titleCase(c)]));
    categoryList.innerHTML = options.map(([value, label]) =>
      // Button: selects a category and immediately re-runs the search.
      '<li><button type="button" data-category="' + value + '" class="' + (value === selectedCategory ? 'active' : '') + '">' + label + '</button></li>'
    ).join('');
  }

  async function applyFilters() {
    hideMessage(message);
    const params = new URLSearchParams();
    if (form.q.value.trim()) params.set('q', form.q.value.trim());
    if (selectedCategory) params.set('category', selectedCategory);
    if (form.minPrice.value.trim()) params.set('minPrice', form.minPrice.value.trim());
    if (form.maxPrice.value.trim()) params.set('maxPrice', form.maxPrice.value.trim());
    history.replaceState(null, '', 'products.html' + (params.toString() ? '?' + params : ''));
    document.getElementById('catalog-title').textContent = selectedCategory ? titleCase(selectedCategory) : 'All products';

    try {
      const products = await api('/api/products?' + params);
      document.getElementById('result-count').textContent = products.length + (products.length === 1 ? ' product' : ' products');
      grid.innerHTML = products.length
        ? products.map(productCard).join('')
        : '<div class="empty card">' + icon('search') + '<p>No products match these filters.</p></div>';
    } catch (e) {
      grid.innerHTML = '';
      document.getElementById('result-count').textContent = '';
      showMessage(message, 'error', e.message, e.code);
    }
  }

  // Start from the URL (?q=...&category=...&minPrice=...&maxPrice=...)
  const start = new URLSearchParams(location.search);
  form.q.value = start.get('q') || '';
  form.minPrice.value = start.get('minPrice') || '';
  form.maxPrice.value = start.get('maxPrice') || '';
  selectedCategory = (start.get('category') || '').toUpperCase();
  drawCategories();

  form.addEventListener('submit', (event) => { event.preventDefault(); applyFilters(); });
  document.getElementById('filter-reset').addEventListener('click', () => {
    form.reset();
    selectedCategory = '';
    drawCategories();
    applyFilters();
  });
  categoryList.addEventListener('click', (event) => {
    const button = event.target.closest('[data-category]');
    if (!button) return;
    selectedCategory = button.dataset.category;
    drawCategories();
    applyFilters();
  });
  wireAddToCartButtons(grid);
  applyFilters();
})();
