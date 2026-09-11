document.addEventListener('DOMContentLoaded', () => {
  if (Session.isAuthenticated()) {
    window.location.href = '/home';
    return;
  }

  const form = document.getElementById('login-form');
  const alertBox = document.getElementById('login-alert');
  const submitBtn = document.getElementById('login-submit');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    alertBox.classList.add('hidden');

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    submitBtn.disabled = true;
    submitBtn.textContent = 'Ingresando...';

    try {
      const result = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });

      const data = result.data;
      Session.save({
        token: data.token,
        tokenType: data.tokenType,
        idUser: data.idUser,
        username: data.username,
        email: data.email,
        nombreCompleto: data.nombreCompleto,
        rol: data.rol,
      });

      window.location.href = '/home';
    } catch (err) {
      alertBox.textContent = (err && err.error) || (err && err.msg) || 'No fue posible iniciar sesión';
      alertBox.classList.remove('hidden');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Ingresar';
    }
  });
});
