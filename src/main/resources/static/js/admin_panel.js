document.addEventListener("DOMContentLoaded", () => {
	initTabs();
	initLogout();
	cargarUsuarios();
	cargarEventos();
});

/**
 * ---------- CAMBIO DE PESTAÑAS ----------
 */
function initTabs() {
	const tabs = document.querySelectorAll(".menu-item");
	const contents = document.querySelectorAll(".tab-content");

	tabs.forEach((tab) => {
		tab.addEventListener("click", () => {
			tabs.forEach((b) => b.classList.remove("active"));
			tab.classList.add("active");

			const target = tab.dataset.tab;
			contents.forEach((c) => c.classList.remove("active"));
			const content = document.getElementById(target);
			if (content) content.classList.add("active");
		});
	});
}

/**
 * ---------- LOGOUT ----------
 */
function initLogout() {
	const logoutBtn = document.querySelector('.logout-btn');
	if (!logoutBtn) return;

	logoutBtn.addEventListener('click', () => {
		Swal.fire({
			title: '¿Desea cerrar sesión?',
			icon: 'warning',
			showCancelButton: true,
			confirmButtonText: 'Sí, salir',
			cancelButtonText: 'Cancelar'
		}).then(async (result) => {
			if (result.isConfirmed) {
				try {
					await fetch('/usuarios/logout', { method: 'GET', credentials: 'same-origin' });
					window.location.href = '/usuarios/login';
				} catch (error) {
					console.error('Error al cerrar sesión:', error);
					window.location.href = '/usuarios/login';
				}
			}
		});
	});
}

/**
 * ---------- CARGAR USUARIOS ----------
 */
async function cargarUsuarios() {
	const cont = document.getElementById("listaUsuarios");
	if (!cont) return;

	try {
		const res = await fetch("/usuarios/todos");
		if (!res.ok) throw new Error("No se pudieron obtener los usuarios");
		const usuarios = await res.json();

		cont.innerHTML = usuarios
			.map(u => `
        <div class="ticket-card">
          <div class="ticket-info" style="display:flex; justify-content: space-between; align-items:center;">
            <div>
              <h3>${u.nombre}</h3>
              <p>Email: ${u.email}</p>
              <p>Rol: ${u.rol.nombre}</p>
            </div>
            ${u.rol.nombre !== 'ADMIN' ? `<button class="btn-delete-user" data-email="${u.email}">Eliminar</button>` : ''}
          </div>
        </div>
      `)
			.join('');

		// Asignar eventos a los botones de eliminar
		cont.querySelectorAll('.btn-delete-user').forEach((btn) => {
			btn.addEventListener('click', function() {
				let email = this.dataset.email;
				eliminarUsuario(email)
			})
		});
	} catch (err) {
		console.error(err);
		cont.innerHTML = "<p>Error al cargar los usuarios.</p>";
	}
}

/**
 * ---------- ELIMINAR USUARIO ----------
 */
async function eliminarUsuario(email) {
	Swal.fire({
		title: '¿Desea eliminar este usuario?',
		icon: 'warning',
		showCancelButton: true,
		confirmButtonText: 'Sí, eliminar',
		cancelButtonText: 'Cancelar'
	}).then(async (result) => {
		if (!result.isConfirmed) return;

		try {
			const res = await fetch(`/usuarios/eliminar/${email}`, { method: 'DELETE', credentials: 'same-origin' });
			if (!res.ok) throw new Error(await res.text() || "Error al eliminar usuario");

			Swal.fire({ icon: 'success', title: 'Usuario eliminado', showConfirmButton: false, timer: 1500 });
			cargarUsuarios();
		} catch (err) {
			console.error(err);
			Swal.fire({ icon: 'error', title: 'No se pudo eliminar', text: err.message });
		}
	});
}

/**
 * ---------- CARGAR EVENTOS ----------
 */
async function cargarEventos() {
	const cont = document.getElementById("listaEventos");
	if (!cont) return;

	try {
		const res = await fetch("/eventos/todos");
		if (!res.ok) throw new Error("No se pudieron obtener los eventos");
		const eventos = await res.json();

		if (eventos.length === 0) {
			cont.innerHTML = "<p>No hay eventos creados.</p>";
			return;
		}

		cont.innerHTML = eventos
			.map(e => `
        <div class="ticket-card">
          <img src="${e.imagenUrl || '/images/default-event.jpg'}" class="ticket-image" alt="${e.titulo}">
          <div class="ticket-info">
            <div class="ticket-header">
              <h3>${e.titulo}</h3>
              <span class="badge">${e.categoria}</span>
            </div>
            <div class="ticket-details">
              <p>${e.descripcion}</p>
              <p class="detail-item">Fecha: ${e.fecha} ${e.hora}</p>
            </div>
          </div>
        </div>
      `)
			.join("");
	} catch (err) {
		console.error(err);
		cont.innerHTML = "<p>Error al cargar los eventos.</p>";
	}
}

/**
 * ---------- CREAR EVENTO ----------
 */
const formEvento = document.getElementById("formCrearEvento");
if (formEvento) {
	formEvento.addEventListener("submit", async (e) => {
		e.preventDefault();
		const data = new FormData(formEvento);

		try {
			const res = await fetch("/eventos/crear", { method: 'POST', body: data });
			if (!res.ok) throw new Error(await res.text() || "Error al crear el evento");

			Swal.fire({ icon: 'success', title: 'Evento creado', showConfirmButton: false, timer: 1500 });
			formEvento.reset();
			cargarEventos();
		} catch (err) {
			console.error(err);
			Swal.fire({ icon: 'error', title: 'No se pudo crear el evento', text: err.message });
		}
	});
}
