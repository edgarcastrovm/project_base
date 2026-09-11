/**
 * Utilidades comunes: sesión en sessionStorage, fetch autenticado con
 * requestId por petición (se propaga hasta el filtro/log4j2 del backend)
 * y control del topmenu.
 */
const SESSION_KEY = 'final01_session';

function generateRequestId() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID();
  }
  return 'req-' + Date.now() + '-' + Math.random().toString(16).slice(2);
}

const Session = {
  save(session) {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  },
  get() {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  clear() {
    sessionStorage.removeItem(SESSION_KEY);
  },
  isAuthenticated() {
    const session = Session.get();
    return !!(session && session.token);
  },
};

/**
 * Wrapper de fetch que agrega el token JWT, un X-Request-Id por petición
 * y normaliza la respuesta estandarizada {code, msg, data, error}.
 */
async function apiFetch(path, options = {}) {
  const session = Session.get();
  const requestId = generateRequestId();

  const headers = Object.assign(
    {
      'Content-Type': 'application/json',
      'X-Request-Id': requestId,
    },
    options.headers || {}
  );

  if (session && session.token) {
    headers['Authorization'] = 'Bearer ' + session.token;
  }

  let response;
  try {
    response = await fetch(window.API_BASE_URL + path, {
      ...options,
      headers,
    });
  } catch (networkError) {
    throw { code: 0, msg: 'No se pudo conectar con el servidor', error: networkError.message, requestId };
  }

  let body = null;
  try {
    body = await response.json();
  } catch (parseError) {
    // respuesta sin body (ej. 204)
  }

  if (response.status === 401) {
    Session.clear();
    window.location.href = '/login';
    throw { code: 401, msg: 'Sesión expirada', error: 'No autorizado', requestId };
  }

  if (!response.ok) {
    const errBody = body || {};
    throw {
      code: errBody.code || response.status,
      msg: errBody.msg || 'Ocurrió un error',
      error: errBody.error || response.statusText,
      requestId,
    };
  }

  return body;
}

function requireAuth() {
  if (!Session.isAuthenticated()) {
    window.location.href = '/login';
  }
}

function initials(name) {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');
}

function renderTopmenuUser() {
  const session = Session.get();
  if (!session) return;

  const nameEl = document.getElementById('topmenu-username');
  const roleEl = document.getElementById('topmenu-role');
  const avatarEl = document.getElementById('topmenu-avatar');

  const displayName = session.nombreCompleto && session.nombreCompleto.trim() ? session.nombreCompleto : session.username;

  if (nameEl) nameEl.textContent = displayName;
  if (roleEl) roleEl.textContent = session.rol || '';
  if (avatarEl) avatarEl.textContent = initials(displayName);
}

function setupLogout() {
  const btn = document.getElementById('btn-logout');
  if (btn) {
    btn.addEventListener('click', () => {
      Session.clear();
      window.location.href = '/login';
    });
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderTopmenuUser();
  setupLogout();
});
