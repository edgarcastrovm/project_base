document.addEventListener('DOMContentLoaded', async () => {
  requireAuth();
  const session = Session.get();
  if (!session) return;

  document.getElementById('home-welcome').textContent = 'Bienvenido, ' + (session.nombreCompleto || session.username);

  try {
    const [usersRes, rolesRes] = await Promise.all([
      apiFetch('/usuarios'),
      apiFetch('/roles'),
    ]);

    const users = usersRes.data || [];
    const roles = rolesRes.data || [];

    document.getElementById('stat-total-users').textContent = users.length;
    document.getElementById('stat-active-users').textContent = users.filter((u) => u.activo).length;
    document.getElementById('stat-total-roles').textContent = roles.length;
  } catch (err) {
    console.warn('No fue posible cargar el resumen', err);
  }
});
