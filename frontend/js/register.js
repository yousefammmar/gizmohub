/* register.js — Registration form. API: POST /api/auth/register. Depends on app.js. Analytics: none.
   No validation is duplicated here on purpose: the backend rules AUTH-01..04 answer every case. */
(async function () {
  await GH.ready;
  const form = document.getElementById('register-form');
  const message = document.getElementById('register-message');

  form.addEventListener('submit', async function handleRegister(event) {
    event.preventDefault();
    hideMessage(message);
    try {
      await api('/api/auth/register', { method: 'POST', body: {
        email: form.email.value,
        password: form.password.value,
        confirmPassword: form.confirmPassword.value
      } });
      window.location.href = 'login.html?registered=1';
    } catch (e) {
      showMessage(message, 'error', e.message, e.code);
    }
  });
})();
