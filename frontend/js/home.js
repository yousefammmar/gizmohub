/* home.js — Home page: perks, category tiles and 8 featured in-stock products.
   API: GET /api/products. Depends on app.js. Analytics: none. */
(async function () {
  await GH.ready;
  document.getElementById('hero-art').innerHTML = icon('LAPTOPS');

  document.getElementById('perks').innerHTML = [
    ['truck', 'Free shipping from $50', 'Orders under $50 ship for a flat $5.'],
    ['shield', 'Secure accounts', 'Passwords are hashed, sessions expire after 30 minutes.'],
    ['refresh', 'Live stock', 'You can only order what is really on the shelf.']
  ].map(([name, title, text]) =>
    '<div class="perk card">' + icon(name) + '<div><strong>' + title + '</strong><div class="muted">' + text + '</div></div></div>'
  ).join('');

  const categories = await api('/api/products/categories');
  document.getElementById('category-tiles').innerHTML = categories.map((c) =>
    '<a class="category-tile" href="products.html?category=' + c + '">' + icon(c) + titleCase(c) + '</a>'
  ).join('');

  const grid = document.getElementById('featured-grid');
  const products = await api('/api/products');
  grid.innerHTML = products.filter((p) => p.inStock).slice(0, 8).map(productCard).join('');
  wireAddToCartButtons(grid);
})();
