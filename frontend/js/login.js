/* login.js — Login form. API: POST /api/auth/login. Depends on app.js. Analytics: none.
   After login the user returns to ?next=... (only local .html pages are accepted). */
(async function () {
  await GH.ready;
  const form = document.getElementById('login-form');
  const message = document.getElementById('login-message');
  const params = new URLSearchParams(location.search);
  if (params.get('registered')) showMessage(message, 'success', 'Account created. Please log in.');

  // Only allow redirects to our own pages (prevents open-redirect via ?next=https://evil.example)
  const next = params.get('next');
  const target = next && /^[a-z-]+\.html(\?[\w=&%-]*)?$/.test(next) ? next : 'products.html';

  form.addEventListener('submit', async function handleLogin(event) {
    event.preventDefault();
    hideMessage(message);
    try {
      await api('/api/auth/login', { method: 'POST', body: {
        email: form.email.value,
        password: form.password.value
      } });
      window.location.href = target;
    } catch (e) {
      showMessage(message, 'error', e.message, e.code);
    }
  });
})();
