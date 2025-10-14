const loginForm = document.getElementById('loginForm');
const mensaje = document.getElementById('mensaje');
const perfilDiv = document.getElementById('perfil');
const logoutBtn = document.getElementById('logoutBtn');

const API_LOGIN = '/auth/login';
const API_PERFIL = '/usuarios/me';

// Función para mostrar perfil
function mostrarPerfil(perfil) {
    document.getElementById('nombre').textContent = `Nombre: ${perfil.nombre}`;
    document.getElementById('email').textContent = `Email: ${perfil.email}`;
    document.getElementById('rol').textContent = `Rol: ${perfil.rol}`;
    perfilDiv.style.display = 'block';
    loginForm.style.display = 'none';
}

// Cerrar sesión
logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('jwt');
    perfilDiv.style.display = 'none';
    loginForm.style.display = 'block';
    mensaje.textContent = '';
});

// Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    mensaje.textContent = '';

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        // 1️⃣ Login
        const loginResponse = await fetch(API_LOGIN, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!loginResponse.ok) {
            const errorData = await loginResponse.text();
            mensaje.textContent = `Error: ${errorData}`;
            return;
        }

        const data = await loginResponse.json();
        const token = data.token; // backend devuelve { token: "..." }
        localStorage.setItem('jwt', token);

        // Obtener perfil automáticamente
        await cargarPerfil(token);

    } catch (err) {
        mensaje.textContent = 'Error en la conexión con el servidor';
        console.error(err);
    }
});

// Cargar perfil usando el token
async function cargarPerfil(token) {
    try {
        const perfilResponse = await fetch(API_PERFIL, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (perfilResponse.status === 401) {
            mensaje.textContent = 'Token inválido o expirado. Por favor, inicia sesión de nuevo.';
            localStorage.removeItem('jwt');
            return;
        }

        if (!perfilResponse.ok) {
            mensaje.textContent = 'No se pudo obtener el perfil.';
            return;
        }

        const perfil = await perfilResponse.json();
        mostrarPerfil(perfil);

    } catch (err) {
        mensaje.textContent = 'Error al obtener perfil';
        console.error(err);
    }
}

// Comprobar si hay token al cargar la página
window.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwt');
    if (token) {
        cargarPerfil(token);
    }
});
