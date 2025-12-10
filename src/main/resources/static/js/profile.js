'use strict';

/**
 * ======================================
 * PERFIL DE USUARIO - EVENTZONE
 * Controla la carga, edición y actualización del perfil de usuario,
 * la navegación entre pestañas, el manejo de favoritos y la sesión.
 * ======================================
 */

/* ====== VARIABLES GLOBALES ====== */
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

/* ====== INICIALIZACIÓN ====== */
window.addEventListener('DOMContentLoaded', () => {
	navTab();
	editarPerfil();
	guardarCambios();
	cancelarEdit();
	logout();
});

cargarPerfil();

/* =========================================================
   CARGAR PERFIL DESDE EL SERVIDOR
   ========================================================= */
/**
 * Obtiene los datos del perfil del usuario autenticado y los muestra en el formulario.
 * @async
 * @function cargarPerfil
 */
async function cargarPerfil() {
	try {
		const res = await fetch("/usuarios/me", {
			method: "GET",
			credentials: "include"
		});

		if (!res.ok) throw new Error("No autenticado");

		const data = await res.json();

		// Cargar datos en los campos
		nombreInput.value = data.nombre;
		emailInput.value = data.email;
		telefonoInput.value = data.telefono || '';
		ciudadInput.value = data.ciudad || '';
		bioTextarea.value = data.bio || '';
		fechaNacimientoInput.value = data.fechaNacimiento || '';

		// Actualizar barra lateral
		document.getElementById('userName').textContent = data.nombre;
		document.getElementById('userEmail').textContent = data.email;
		const initials = data.nombre.split(' ').map(n => n[0]).join('').toUpperCase();
		document.getElementById('userAvatar').textContent = initials;

	} catch (error) {
		console.error(error);
		window.location.href = "/usuarios/login";
	}
}

/* =========================================================
   CARGA DINÁMICA DE EVENTOS DISPONIBLES
   ========================================================= */
window.addEventListener('DOMContentLoaded', () => {
	cargarEventosDisponibles();
});

/**
 * Obtiene los eventos disponibles desde el servidor y los renderiza en la pestaña correspondiente.
 * @async
 * @function cargarEventosDisponibles
 */
async function cargarEventosDisponibles() {
	const eventsContainer = document.getElementById('availableEventsList');
	const noEventsMsg = document.getElementById('noAvailableEvents');

	try {
		const res = await fetch('/eventos/disponibles', {
			method: 'GET',
			credentials: 'include'
		});

		if (!res.ok) throw new Error('Error al obtener eventos');

		const eventos = await res.json();

		// Limpiar contenedor
		eventsContainer.innerHTML = '';

		if (!eventos || eventos.length === 0) {
			noEventsMsg.style.display = 'block';
			return;
		} else {
			noEventsMsg.style.display = 'none';
		}

		function formatFecha(fechaStr) {
			if (!fechaStr) return '';
			const fecha = new Date(fechaStr);
			const meses = [
				"Ene", "Feb", "Mar", "Abr", "May", "Jun",
				"Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
			];
			const day = String(fecha.getDate()).padStart(2, '0');
			const month = meses[fecha.getMonth()];
			const year = fecha.getFullYear();

			return `${day}-${month}-${year}`;
		}


		// Crear tarjetas de eventos
		eventos.forEach(evento => {
			const card = document.createElement('div');
			card.classList.add('ticket-card');

			card.innerHTML = `
                <img src="${evento.imagenUrl}" alt="${evento.nombre}" class="ticket-image">

                <div class="ticket-info">
                    <div class="ticket-header">
                        <h3>${evento.nombre}</h3>
                        <span class="badge badge-success">Disponible</span>
                    </div>

                    <div class="ticket-details">
					<p class="detail-item">
					    <span class="detail-icon">📅</span>
					    ${evento.fechaFin && evento.fechaFin !== evento.fecha
					? `${formatFecha(evento.fecha)} - ${formatFecha(evento.fechaFin)}`
					: formatFecha(evento.fecha)
				}
					</p>

                        <p class="detail-item">
                            <span class="detail-icon">📍</span> ${evento.ciudad}, ${evento.direccion}
                        </p>
						<p class="detail-item price">
						    <span class="detail-icon">💳</span>
						    ${evento.tickets && evento.tickets.length > 0
					? evento.tickets.map(t => `${t.tipo}: ${t.precio} €`).join(' | ')
					: 'No hay tickets'}
						</p>

                    </div>

                    <div class="ticket-actions">
                        <a href="/eventos/${evento.id}" class="btn-primary">Ver detalles</a>
                        <a href="/eventos/comprar/${evento.id}" class="btn-secondary">Comprar</a>
                    </div>
                </div>
            `;

			eventsContainer.appendChild(card);
		});

		// Animación de entrada
		const cards = eventsContainer.querySelectorAll('.ticket-card');
		cards.forEach((card, index) => {
			card.style.opacity = '0';
			card.style.transform = 'translateY(20px)';
			card.style.transition = 'all 0.5s ease';

			setTimeout(() => {
				card.style.opacity = '1';
				card.style.transform = 'translateY(0)';
			}, 50 + (index * 100));
		});

	} catch (error) {
		console.error('Error cargando eventos disponibles:', error);
		noEventsMsg.textContent = 'No se pudieron cargar los eventos.';
		noEventsMsg.style.display = 'block';
	}
}

/* =========================================================
   CARGAR ENTRADAS DEL USUARIO
   ========================================================= */
window.addEventListener('DOMContentLoaded', () => {
	cargarMisEntradas();
});

/**
 * Obtiene las entradas compradas por el usuario autenticado.
 * @async
 * @function cargarMisEntradas
 */
async function cargarMisEntradas() {

	const ticketsContainer = document.querySelector('#ticketsTab .tickets-list');

	try {
		const res = await fetch('/entradas/mias', {
			method: 'GET',
			credentials: 'include'
		});

		if (!res.ok) throw new Error('Error al obtener entradas');

		const entradas = await res.json();

		ticketsContainer.innerHTML = '';

		if (entradas.length === 0) {
			ticketsContainer.innerHTML = `
                <p class="no-events">No tienes entradas compradas.</p>
            `;
			return;
		}

		entradas.forEach(ticket => {
			const card = document.createElement('div');
			card.classList.add('ticket-card');

			const estadoBadge =
				ticket.estado === 'Confirmado'
					? '<span class="badge badge-success">Confirmado</span>'
					: '<span class="badge badge-pending">Pendiente</span>';

			function formatFecha(fechaStr) {
				const fecha = new Date(fechaStr);
				return fecha.toLocaleDateString('es-ES', {
					day: '2-digit',
					month: 'short',
					year: 'numeric'
				});
			}

			card.innerHTML = `
                <img src="${ticket.imagenUrl}" 
                     alt="${ticket.eventoNombre}" 
                     class="ticket-image">

                <div class="ticket-info">
                    <div class="ticket-header">
                        <h3>${ticket.eventoNombre}</h3>
                        ${estadoBadge}
                    </div>

                    <div class="ticket-details">
                        <p class="detail-item">
                            <span class="detail-icon">📅</span>
                            ${formatFecha(ticket.fecha)}
                        </p>
                        <p class="detail-item">
                            <span class="detail-icon">📍</span>
                            ${ticket.ciudad}, ${ticket.lugar}
                        </p>
                        <p class="detail-item price">
                            <span class="detail-icon">💳</span>
                            €${ticket.precio}
                        </p>
                    </div>

                    <div class="ticket-actions">
                        <a href="/entradas/${ticket.id}" class="btn-primary">Ver entrada</a>
                        <a href="/entradas/${ticket.id}/descargar" class="btn-secondary">Descargar</a>
                    </div>
                </div>
            `;

			ticketsContainer.appendChild(card);
		});

	} catch (error) {
		console.error('Error cargando entradas:', error);
		ticketsContainer.innerHTML = `<p class="no-events">No se pudieron cargar las entradas.</p>`;
	}
}



/* =========================================================
   NAVEGACIÓN ENTRE PESTAÑAS
   ========================================================= */
/**
 * Controla la navegación entre las diferentes secciones del perfil.
 * @function navTab
 */
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

/* =========================================================
   EDICIÓN DE PERFIL
   ========================================================= */
/**
 * Habilita los campos del formulario para su edición.
 * @function editarPerfil
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

		[nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
			.forEach(el => el.removeAttribute('readonly'));

		nombreInput.focus();
		editBtn.style.display = 'none';
		editActions.style.display = 'flex';
	});
}

/* =========================================================
   GUARDAR CAMBIOS DEL PERFIL
   ========================================================= */
/**
 * Envía los datos actualizados del perfil al servidor y muestra una notificación con SweetAlert2.
 * @async
 * @function guardarCambios
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

			// Actualizar vista
			document.getElementById('userName').textContent = nombreInput.value;
			document.getElementById('userEmail').textContent = emailInput.value;
			const initials = nombreInput.value.split(' ').map(n => n[0]).join('').toUpperCase();
			document.getElementById('userAvatar').textContent = initials;

			// Restaurar interfaz
			[nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
				.forEach(el => el.setAttribute('readonly', true));
			editBtn.style.display = 'flex';
			editActions.style.display = 'none';

			// Alerta de éxito
			Swal.fire({
				icon: 'success',
				title: '¡Perfil actualizado!',
				showConfirmButton: false,
				timer: 2000
			});

		} catch (error) {
			console.error(error);
			Swal.fire({
				icon: 'error',
				title: 'Error al guardar cambios',
				text: error.message
			});
		}
	});
}

/* =========================================================
   CANCELAR EDICIÓN
   ========================================================= */
/**
 * Restaura los valores originales del perfil y bloquea los campos nuevamente.
 * @function cancelarEdit
 */
function cancelarEdit() {
	cancelBtn.addEventListener('click', () => {
		nombreInput.value = originalData.nombre;
		emailInput.value = originalData.email;
		telefonoInput.value = originalData.telefono;
		ciudadInput.value = originalData.ciudad;
		fechaNacimientoInput.value = originalData.fechaNacimiento;
		bioTextarea.value = originalData.bio;

		[nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
			.forEach(el => el.setAttribute('readonly', true));

		editBtn.style.display = 'flex';
		editActions.style.display = 'none';

		Swal.fire({
			icon: 'info',
			title: 'Edición cancelada',
			showConfirmButton: false,
			timer: 1500
		});
	});
}

/* =========================================================
   CERRAR SESIÓN
   ========================================================= */
/**
 * Muestra confirmación antes de cerrar sesión.
 * @function logout
 */
function logout() {
	document.querySelector('.logout-btn').addEventListener('click', () => {
		Swal.fire({
			title: '¿Desea cerrar sesión?',
			icon: 'warning',
			showCancelButton: true,
			confirmButtonText: 'Sí, salir',
			cancelButtonText: 'Cancelar'
		}).then((result) => {
			if (result.isConfirmed) {
				window.location.href = '/usuarios/login';
			}
		});
	});
}

/* =========================================================
   ANIMACIONES DE ENTRADA
   ========================================================= */
/**
 * Añade una animación de entrada para las tarjetas de entrada.
 * @function animaciones
 */
window.addEventListener('load', () => {
	const cards = document.querySelectorAll('.ticket-card');
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
