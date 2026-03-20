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
	navTab();            // navegación entre pestañas
	editarPerfil();      // habilitar edición
	guardarCambios();    // guardar cambios
	cancelarEdit();      // cancelar edición
	logout();            // cerrar sesión
	cargarPerfil();      // cargar datos del usuario
	cargarEventosDisponibles(); // cargar lista de eventos
	cargarMisEntradas(); // cargar las entradas compradas
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
                        <a href="/eventos/detalle/${evento.id}" class="btn-primary">Ver detalles</a>
                        <button class="btn-secondary buy-btn" data-evento-id="${evento.id}">Comprar</button>
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
async function cargarMisEntradas() {
	const ticketsContainer = document.querySelector('#ticketsTab .tickets-list');

	try {
		const res = await fetch('/compras/mis-compras', {
			method: 'GET',
			credentials: 'include'
		});

		if (!res.ok) throw new Error('Error al obtener entradas');

		const compras = await res.json();
		console.log('Compras del usuario:', compras); // Depuración

		// Actualizar el contador de entradas en la barra lateral
		const ticketsCountElement = document.getElementById('ticketsCount');
		if (ticketsCountElement) {
			ticketsCountElement.textContent = compras.length;
		}

		ticketsContainer.innerHTML = '';

		if (!compras || compras.length === 0) {
			ticketsContainer.innerHTML = `<p class="no-events">No tienes entradas compradas.</p>`;
			return;
		}

		compras.forEach(compra => {
			const card = document.createElement('div');
			card.classList.add('ticket-card');

			// Tarjeta de entrada con botón de descarga QR
			card.innerHTML = `
                <img src="${compra.eventoImagenUrl}" alt="${compra.eventoNombre}" class="ticket-image">

                <div class="ticket-info">
                    <div class="ticket-header">
                        <h3>${compra.eventoNombre}</h3>
                        <span class="badge badge-success">Comprado</span>
                    </div>

                    <div class="ticket-details">
                        <p class="detail-item">📅 ${new Date(compra.fechaCompra).toLocaleDateString('es-ES')}</p>
                        <p class="detail-item">📍 ${compra.eventoCiudad}, ${compra.eventoDireccion}</p>
                        <p class="detail-item price">💳 €${compra.ticketPrecio}</p>
                    </div>

                    <div class="ticket-actions">
                        <button class="btn-secondary download-qr-btn">Descargar QR</button>
                    </div>
                </div>
            `;

			ticketsContainer.appendChild(card);

			// Listener para generar QR
			const downloadBtn = card.querySelector('.download-qr-btn');
			downloadBtn.addEventListener('click', async () => {
				try {
					const eventoNombre = compra.eventoNombre || 'Desconocido';
					const compraId = compra.compraId || 'Desconocido';
					const fechaCompra = compra.fechaCompra
						? new Date(compra.fechaCompra).toLocaleDateString('es-ES')
						: 'Desconocida';

					const qrData = `Evento: ${eventoNombre} | Compra ID: ${compraId} | Fecha: ${fechaCompra}`;

					const canvas = document.createElement('canvas');
					await QRCode.toCanvas(canvas, qrData, { width: 300 });

					const link = document.createElement('a');
					link.href = canvas.toDataURL('image/png');
					link.download = `QR_Compra_${compraId}.png`;
					link.click();

				} catch (error) {
					console.error('Error generando QR:', error);
					Swal.fire({
						icon: 'error',
						title: 'Error',
						text: 'No se pudo generar el QR.'
					});
				}
			});
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
			
			if(tabName==='tickets'){
				cargarMisEntradas();
			}
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

/* =========================================================
   BOTÓN COMPRAR TICKET (solo 1 por compra)
   ========================================================= */
document.addEventListener('click', async (e) => {
	if (e.target && e.target.classList.contains('buy-btn')) {
		const ticketId = e.target.dataset.eventoId;

		// Confirmación de compra
		const { isConfirmed } = await Swal.fire({
			title: '¿Desea comprar este ticket?',
			icon: 'question',
			showCancelButton: true,
			confirmButtonText: 'Sí, comprar',
			cancelButtonText: 'Cancelar'
		});

		if (!isConfirmed) return;

		try {
			const res = await fetch(`/compras/ticket/${ticketId}`, {
				method: 'POST',
				credentials: 'include', // si usas cookie JWT HTTP-only
				headers: { 'Content-Type': 'application/json' }
			});

			const data = await res.json();

			if (!res.ok) {
				Swal.fire({
					icon: 'error',
					title: 'Error al comprar',
					text: data.message || 'No se pudo completar la compra'
				});
				return;
			}

			Swal.fire({
				icon: 'success',
				title: 'Compra exitosa',
				html: `Has comprado <strong>1</strong> ticket`,
				timer: 2000,
				showConfirmButton: false
			});

			// Actualizar listas
			cargarEventosDisponibles();
			cargarMisEntradas();

		} catch (error) {
			console.error(error);
			Swal.fire({
				icon: 'error',
				title: 'Error',
				text: 'No se pudo completar la compra'
			});
		}
	}
});

