'use strict'
// ====== VARIABLES ======
const menuItems = document.querySelectorAll('.menu-item');
const tabContents = document.querySelectorAll('.tab-content');
const editBtn = document.getElementById('editBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');
const editActions = document.getElementById('editActions');

const nombreInput = document.getElementById('nombre');
const emailInput = document.getElementById('email');
const telefonoInput = document.getElementById('telefono');
const ciudadInput = document.getElementById('ciudad');
const fechaNacimientoInput = document.getElementById('fechaNacimiento');
const bioTextarea = document.getElementById('bio');

let originalData = {};


// ====== FUNCIONES ======

cargarPerfil();

// ====== INICIALIZACIÓN ======
window.addEventListener('DOMContentLoaded', () => {
	navTab();
	editarPerfil();
	guardarCambios();
	cancelarEdit();
});

// ====== CARGAR PERFIL DESDE LA BD ======
/**
 * Carga los datos del perfil del usuario autenticado desde la base de datos.
 * 
 * - Realiza una petición GET a `/usuarios/me`, incluyendo las cookies (JWT).
 * - Si el usuario está autenticado, recibe un objeto JSON con sus datos.
 * - Rellena los campos del formulario (nombre, email, teléfono, etc.) con los valores recibidos.
 * - También actualiza la información mostrada en la barra lateral (nombre, email, avatar).
 * - Si el token no es válido o no existe, redirige al usuario de vuelta al login.
 * 
 * Flujo general:
 *   1️. Envía petición a `/usuarios/me` → obtiene datos del usuario autenticado.
 *   2️. Actualiza los campos de entrada con los datos del servidor.
 *   3️. Actualiza el panel lateral con nombre, email y avatar iniciales.
 *   4️. Si ocurre un error (no autenticado, fallo del servidor, etc.) → redirige a `/usuarios/login`.
 */
async function cargarPerfil() {
    try {
        const res = await fetch("/usuarios/me", {
            method: "GET",
            credentials: "include"
        });

        if (!res.ok) throw new Error("No autenticado");

        const data = await res.json();

        nombreInput.value = data.nombre;
        emailInput.value = data.email;
        telefonoInput.value = data.telefono || '';
        ciudadInput.value = data.ciudad || '';
        bioTextarea.value = data.bio || '';
        fechaNacimientoInput.value = data.fechaNacimiento || '';
		
        // Sidebar
        document.getElementById('userName').textContent = data.nombre;
        document.getElementById('userEmail').textContent = data.email;
        const initials = data.nombre.split(' ').map(n => n[0]).join('').toUpperCase();
        document.getElementById('userAvatar').textContent = initials;

    } catch (error) {
        console.error(error);
        window.location.href = "/usuarios/login";
    }
}


// ====== NAVEGACIÓN ENTRE TABS ======
/**
 * Gestiona la navegación entre pestañas (tabs) del perfil.
 * 
 * - Escucha los clics en los elementos del menú lateral (menuItems).
 * - Al hacer clic en un tab:
 *    1️. Quita la clase 'active' de todos los elementos del menú y de todas las pestañas.
 *    2. Añade la clase 'active' solo al tab y contenido seleccionado.
 * - Muestra dinámicamente la sección correspondiente según el atributo "data-tab".
 *  */
function navTab() {
	menuItems.forEach(item => {
		item.addEventListener('click', () => {
			menuItems.forEach(mi => mi.classList.remove('active'));
			item.classList.add('active');

			const tabName = item.getAttribute('data-tab');
			tabContents.forEach(tc => tc.classList.remove('active'));

			const selectedTab = document.getElementById(tabName + 'Tab');
			if (selectedTab) selectedTab.classList.add('active');
		});
	});
}


// ====== EDICIÓN DEL PERFIL ======
/**
 * Activa el modo de edición del perfil de usuario.
 * 
 * - Guarda una copia de los datos originales antes de editar (`originalData`).
 * - Quita el atributo `readonly` de todos los campos para permitir su modificación.
 * - Muestra los botones de acción ("Guardar" y "Cancelar") y oculta el botón de "Editar".
 * - Coloca el foco automáticamente en el campo "nombre" para facilitar la edición.
 * 
 * Flujo general:
 *   1️. Al hacer clic en el botón "Editar":
 *        - Se almacenan los valores actuales de los campos (por si se cancela luego).
 *        - Se habilitan los inputs (removiendo `readonly`).
 *        - Se oculta el botón "Editar" y se muestran los botones de guardar/cancelar.
 */
function editarPerfil() {
	editBtn.addEventListener('click', () => {
		originalData = {
			nombre: nombreInput.value,
			email: emailInput.value,
			telefono: telefonoInput.value,
			ciudad: ciudadInput.value,
			fechaNacimiento: fechaNacimientoInput.value,
			bio: bioTextarea.value
		};

		[nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea].forEach(el => el.removeAttribute('readonly'));

		nombreInput.focus();
		editBtn.style.display = 'none';
		editActions.style.display = 'flex';
	});
}

// ====== GUARDAR CAMBIOS ======
/**
 * Guarda los cambios realizados en el perfil de usuario.
 * 
 * - Captura los valores actualizados de los campos del formulario.
 * - Envía una solicitud HTTP PUT a `/usuarios/me` con los nuevos datos en formato JSON.
 * - Incluye las cookies (credenciales) para mantener la sesión.
 * - Si la actualización es exitosa:
 *    → Actualiza la información visible en la interfaz (nombre, email, avatar).
 *    → Muestra un mensaje de confirmación.
 * - Si falla:
 *    → Muestra un mensaje de error y registra el fallo en la consola.
 * 
 * Flujo general:
 *   1️. El usuario hace clic en "Guardar".
 *   2️. Se construye un objeto `payload` con los datos del formulario.
 *   3️. Se envía una petición PUT al backend.
 *   4️. Si la respuesta es correcta, se actualiza la interfaz.
 */
function guardarCambios() {
	saveBtn.addEventListener('click', async () => {
		const payload = {
			nombre: nombreInput.value,
			email: emailInput.value,
			telefono: telefonoInput.value,
			ciudad: ciudadInput.value,
			bio: bioTextarea.value,
			fechaNacimiento: fechaNacimientoInput.value
		};

		try {
			const res = await fetch("/usuarios/me", {
				method: "PUT",
				headers: { "Content-Type": "application/json" },
				credentials: "include",
				body: JSON.stringify(payload)
			});

			if (!res.ok) throw new Error("Error al actualizar perfil");

			// Actualizar UI
			document.getElementById('userName').textContent = nombreInput.value;
			document.getElementById('userEmail').textContent = emailInput.value;
			const initials = nombreInput.value.split(' ').map(n => n[0]).join('').toUpperCase();
			document.getElementById('userAvatar').textContent = initials;

			alert("Perfil actualizado correctamente");
		} catch (error) {
			console.error(error);
			alert("Error al guardar los cambios");
		}
	});
}

// ====== CANCELAR EDICIÓN ======
/**
 * Cancela la edición del perfil y restaura los datos originales.
 * 
 * - Restaura los valores de los campos del formulario al estado original guardado antes de editar.
 * - Vuelve a poner los campos en modo "solo lectura".
 * - Oculta los botones de acción (Guardar / Cancelar) y muestra nuevamente el botón de Editar.
 * 
 * Flujo general:
 *   1️. El usuario hace clic en “Cancelar”.
 *   2️. Se recuperan los valores previos almacenados en `originalData`.
 *   3️. Se deshabilita la edición de los campos (readonly).
 *   4️. Se ajusta la interfaz para volver al modo visualización.
 */
function cancelarEdit() {
	cancelBtn.addEventListener('click', () => {
		// Restaurar los valores originales del perfil
		nombreInput.value = originalData.nombre;
		emailInput.value = originalData.email;
		telefonoInput.value = originalData.telefono;
		ciudadInput.value = originalData.ciudad;
		fechaNacimientoInput.value = originalData.fechaNacimiento;
		bioTextarea.value = originalData.bio;

		// Bloquear nuevamente los campos (solo lectura)
		[nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea].forEach(el => el.setAttribute('readonly', true));

		// Restaurar los botones de acción
		editBtn.style.display = 'flex'; // Mostrar "Editar"
		editActions.style.display = 'none'; // Ocultar "Guardar / Cancelar"
	});
}

// ====== FAVORITOS ======
document.querySelectorAll('.btn-remove').forEach(btn => {
    btn.addEventListener('click', function() {
        if (confirm('¿Deseas eliminar este evento de favoritos?')) {
            const card = this.closest('.favorite-card');
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';
            card.style.transition = 'all 0.3s';
            setTimeout(() => {
                card.remove();
                const favCount = document.getElementById('favoritesCount');
                favCount.textContent = parseInt(favCount.textContent) - 1;
            }, 300);
        }
    });
});

// ====== LOGOUT ======
document.querySelector('.logout-btn').addEventListener('click', () => {
    if (confirm('¿Seguro que deseas cerrar sesión?')) {
        window.location.href = '/usuarios/login';
    }
});

// ====== ANIMACIONES ======
window.addEventListener('load', () => {
    const cards = document.querySelectorAll('.ticket-card, .favorite-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';

        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 50 + (index * 100));
    });
});


