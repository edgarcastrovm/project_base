document.addEventListener('DOMContentLoaded', () => {
  requireAuth();

  const tableBody = document.getElementById('users-tbody');
  const loadingRow = document.getElementById('users-loading');
  const emptyRow = document.getElementById('users-empty');
  const pageAlert = document.getElementById('users-alert');

  const modal = document.getElementById('user-modal');
  const modalTitle = document.getElementById('user-modal-title');
  const form = document.getElementById('user-form');
  const formAlert = document.getElementById('user-form-alert');
  const rolSelect = document.getElementById('idRol');
  const passwordHint = document.getElementById('password-hint');

  let roles = [];
  let editingId = null;

  function showAlert(message, type = 'error') {
    pageAlert.textContent = message;
    pageAlert.className = 'alert ' + (type === 'error' ? 'alert-error' : 'alert-success');
    pageAlert.classList.remove('hidden');
    setTimeout(() => pageAlert.classList.add('hidden'), 4000);
  }

  function showFormAlert(message) {
    formAlert.textContent = message;
    formAlert.classList.remove('hidden');
  }

  async function loadRoles() {
    const res = await apiFetch('/roles');
    roles = res.data || [];
    rolSelect.innerHTML = roles
      .map((r) => `<option value="${r.idRol}">${r.nombre}</option>`)
      .join('');
  }

  function renderRow(user) {
    const estadoBadge = user.activo
      ? '<span class="badge badge-active">Activo</span>'
      : '<span class="badge badge-inactive">Inactivo</span>';

    return `
      <tr data-id="${user.idUser}">
        <td>${user.idUser}</td>
        <td>${user.username}</td>
        <td>${(user.nombres || '') + ' ' + (user.apellidos || '')}</td>
        <td>${user.email}</td>
        <td><span class="badge badge-role">${user.rolNombre || '-'}</span></td>
        <td>${estadoBadge}</td>
        <td>
          <button class="btn btn-secondary btn-sm btn-edit" data-id="${user.idUser}">Editar</button>
        </td>
      </tr>
    `;
  }

  async function loadUsers() {
    loadingRow.classList.remove('hidden');
    emptyRow.classList.add('hidden');
    tableBody.querySelectorAll('tr[data-id]').forEach((row) => row.remove());

    try {
      const res = await apiFetch('/usuarios');
      const users = res.data || [];

      loadingRow.classList.add('hidden');

      if (users.length === 0) {
        emptyRow.classList.remove('hidden');
        return;
      }

      const rowsHtml = users.map(renderRow).join('');
      loadingRow.insertAdjacentHTML('afterend', rowsHtml);

      tableBody.querySelectorAll('.btn-edit').forEach((btn) => {
        btn.addEventListener('click', () => openEditModal(btn.dataset.id, users));
      });
    } catch (err) {
      loadingRow.classList.add('hidden');
      showAlert((err && err.error) || 'No fue posible cargar los usuarios');
    }
  }

  function openCreateModal() {
    editingId = null;
    modalTitle.textContent = 'Nuevo usuario';
    form.reset();
    document.getElementById('username').disabled = false;
    document.getElementById('password').required = true;
    passwordHint.classList.add('hidden');
    formAlert.classList.add('hidden');
    modal.classList.remove('hidden');
  }

  function openEditModal(id, users) {
    const user = users.find((u) => String(u.idUser) === String(id));
    if (!user) return;

    editingId = user.idUser;
    modalTitle.textContent = 'Editar usuario';
    formAlert.classList.add('hidden');

    document.getElementById('username').value = user.username;
    document.getElementById('username').disabled = true;
    document.getElementById('email').value = user.email;
    document.getElementById('nombres').value = user.nombres || '';
    document.getElementById('apellidos').value = user.apellidos || '';
    document.getElementById('password').value = '';
    document.getElementById('password').required = false;
    document.getElementById('idRol').value = user.idRol;
    document.getElementById('activo').checked = !!user.activo;
    passwordHint.classList.remove('hidden');

    modal.classList.remove('hidden');
  }

  function closeModal() {
    modal.classList.add('hidden');
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    formAlert.classList.add('hidden');

    const payload = {
      email: document.getElementById('email').value.trim(),
      nombres: document.getElementById('nombres').value.trim(),
      apellidos: document.getElementById('apellidos').value.trim(),
      idRol: Number(document.getElementById('idRol').value),
    };

    const password = document.getElementById('password').value;
    const submitBtn = document.getElementById('user-form-submit');
    submitBtn.disabled = true;

    try {
      if (editingId) {
        payload.activo = document.getElementById('activo').checked;
        if (password) payload.password = password;
        await apiFetch(`/usuarios/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        });
        showAlert('Usuario actualizado correctamente', 'success');
      } else {
        payload.username = document.getElementById('username').value.trim();
        payload.password = password;
        await apiFetch('/usuarios', {
          method: 'POST',
          body: JSON.stringify(payload),
        });
        showAlert('Usuario creado correctamente', 'success');
      }

      closeModal();
      await loadUsers();
    } catch (err) {
      showFormAlert((err && err.error) || (err && err.msg) || 'No fue posible guardar el usuario');
    } finally {
      submitBtn.disabled = false;
    }
  });

  document.getElementById('btn-new-user').addEventListener('click', openCreateModal);
  document.getElementById('user-modal-close').addEventListener('click', closeModal);
  document.getElementById('user-modal-cancel').addEventListener('click', closeModal);
  modal.addEventListener('click', (event) => {
    if (event.target === modal) closeModal();
  });

  (async () => {
    try {
      await loadRoles();
    } catch (err) {
      showAlert('No fue posible cargar los roles');
    }
    await loadUsers();
  })();
});
