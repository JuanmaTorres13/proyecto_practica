document.addEventListener("DOMContentLoaded", () => {
	initTabs();
	initLogout();
	cargarUsuarios();
	cargarEventos();
	llamarEvento();
	
	document.querySelectorAll('input[name="eventType"]').forEach(radio => {
	    radio.addEventListener("change", () => {
	        mostrarCamposPorTipo(radio.value.toLowerCase());
	    });
	});

	const btnAdd = document.getElementById("addTicketBtn");
	if (btnAdd) btnAdd.addEventListener("click", agregarTicket);
});

let archivoImagen1 = null; // variable global para guardar el archivo seleccionado

const imagenInput = document.getElementById("imagenFile");
if (imagenInput) {
    imagenInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        archivoImagen1 = file || null;

        const imagePreview = document.getElementById("imagePreview");
        if (imagePreview) {
            if (file) {
                imagePreview.src = URL.createObjectURL(file); // previsualiza la nueva imagen
            } else {
                imagePreview.src = document.getElementById("eventForm").dataset.imagenUrl || '/images/default-image.jpg';
            }
        }
    });
}


/**
 * ---------- CAMBIO DE PESTAÑAS ----------
 */
function initTabs() {
	const tabs = document.querySelectorAll(".menu-item");
	const contents = document.querySelectorAll(".tab-content");

	tabs.forEach(tab => {
		tab.addEventListener("click", () => {
			tabs.forEach(b => b.classList.remove("active"));
			tab.classList.add("active");

			const target = tab.dataset.tab;
			contents.forEach(c => c.classList.remove("active"));
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
		}).then(async result => {
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

		cont.innerHTML = usuarios.map(u => `
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
        `).join('');

		cont.querySelectorAll('.btn-delete-user').forEach(btn => {
			btn.addEventListener('click', () => eliminarUsuario(btn.dataset.email));
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
	}).then(async result => {
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

/**
 * ---------- CARGAR EVENTOS ----------
 */
async function cargarEventos() {
	const cont = document.getElementById("listaEventos");
	if (!cont) return;

	try {
		const res = await fetch("/eventos/disponibles");
		if (!res.ok) throw new Error("No se pudieron obtener los eventos");
		const eventos = await res.json();

		if (eventos.length === 0) {
			cont.innerHTML = "<p class='empty-list'>No hay eventos creados.</p>";
			return;
		}

		cont.innerHTML = eventos.map(e => {
			let badgeClass = "badge-default";
			let badgeText = e.tipo || "Evento";

			if (badgeText.toLowerCase() === "cine") badgeClass = "badge-cine";
			else if (badgeText.toLowerCase() === "concierto") badgeClass = "badge-concierto";
			else if (badgeText.toLowerCase() === "festival") badgeClass = "badge-festival";

			return `
            <div class="ticket-card" data-id="${e.id}" data-tipo="${e.tipo}">
                <div class="ticket-info">
                    <div class="ticket-avatar-section">
                        <img src="${e.imagenUrl || '/images/default-event.jpg'}" alt="${e.nombre}" class="ticket-avatar">
                    </div>
                    <div class="ticket-details">
                        <div class="ticket-header">
                            <h3>${e.nombre}</h3>
                            <span class="badge ${badgeClass}">${badgeText.toUpperCase()}</span>
                        </div>
                        <p>Fecha: ${e.fechaFin && e.fechaFin !== e.fecha ? `${formatFecha(e.fecha)} - ${formatFecha(e.fechaFin)}` : formatFecha(e.fecha)}</p>
                        <p>📍 ${e.ciudad || ''}${e.direccion ? ", " + e.direccion : ''}</p>
                    </div>
                    <div class="ticket-actions">
                        <button class="btn-delete-user btn-edit">✏️ Editar</button>
                        <button class="btn-delete-user btn-delete">🗑️ Eliminar</button>
                    </div>
                </div>
            </div>`;
		}).join("");

		// Eliminar evento
		cont.querySelectorAll(".btn-delete").forEach(btn => {
			btn.addEventListener("click", async () => {
				const card = btn.closest(".ticket-card");
				const id = card.dataset.id;
				if (!id) return;

				const confirm = await Swal.fire({
					title: "¿Eliminar este evento?",
					icon: "warning",
					showCancelButton: true,
					confirmButtonText: "Sí, eliminar",
					cancelButtonText: "Cancelar"
				});

				if (confirm.isConfirmed) {
					try {
						const res = await fetch(`/eventos/eliminar/${id}`, { method: "DELETE" });
						if (!res.ok) throw new Error(await res.text() || "Error al eliminar evento");
						Swal.fire({ icon: "success", title: "Evento eliminado", showConfirmButton: false, timer: 1500 });
						card.remove();
					} catch (err) {
						console.error(err);
						Swal.fire({ icon: "error", title: "No se pudo eliminar", text: err.message });
					}
				}
			});
		});

		// Editar evento
		cont.querySelectorAll(".btn-edit").forEach(btn => {
			btn.addEventListener("click", async () => {

				const card = btn.closest(".ticket-card");
				const id = card.dataset.id;
				const tipo = card.dataset.tipo;
				console.log("ID del evento:", id);

				if (!id) return;

				try {
					const res = await fetch(`/eventos/${id}`);
					if (!res.ok) throw new Error(await res.text());
					const evento = await res.json();
					llenarFormularioEvento(evento);

					const editTab = document.querySelector('.menu-item[data-tab="tab-crear-evento"]');
					if (editTab) editTab.click();

					// Guardar ID de evento en el formulario para editar
					const form = document.getElementById("eventForm");
					form.dataset.eventoid = id;

				} catch (err) {
					console.error(err);
					Swal.fire({ icon: "error", title: "No se pudo cargar evento", text: err.message });
				}
			});
		});
	} catch (err) {
		console.error(err);
		cont.innerHTML = "<p class='empty-list'>Error al cargar los eventos.</p>";
	}
}

/**
 * ---------- FUNCION LLENAR FORMULARIO ----------
 */
function llenarFormularioEvento(evento) {
	document.querySelectorAll('input[name="eventType"]').forEach(r => r.checked = false);
	if (evento.tipo) {
		const tipoInput = document.querySelector(`input[name="eventType"][value="${evento.tipo}"]`);
		if (tipoInput) tipoInput.checked = true;
	}

	
	// Guardar la URL de la imagen actual
	const form = document.getElementById("eventForm");
	form.dataset.imagenUrl = evento.imagenUrl || '/images/default-image.jpg';

	// Mostrar la imagen en la previsualización
	const imagePreview = document.getElementById("imagePreview");
	if (imagePreview) {
	    imagePreview.src = evento.imagenUrl || '/images/default-image.jpg';
	}

	// Opcional: quitar required al editar
	const imagenInput = document.getElementById("imagenFile");
	if (imagenInput) {
	    imagenInput.required = !evento.imagenUrl; // solo requerido si no hay imagen
	}
	
	
	document.getElementById("eventName").value = evento.nombre || "";
	document.getElementById("eventDescription").value = evento.descripcion || "";
	document.getElementById("eventCity").value = evento.ciudad || "";
	document.getElementById("eventAddress").value = evento.direccion || "";
	document.getElementById("eventDate").value = evento.fecha || "";
	document.getElementById("eventContact").value = evento.contactoEmail || "";

	mostrarCamposPorTipo(evento.tipo);

	// Campos específicos
	if (evento.tipo === "cine") {
		document.getElementById("movieTitle").value = evento.tituloPelicula || "";
		document.getElementById("movieDirector").value = evento.director || "";
		document.getElementById("movieRating").value = evento.clasificacion || "";
		document.getElementById("movieLanguage").value = evento.idioma || "";
		document.getElementById("movieSala").value = evento.sala || "";
		document.getElementById("movieAsientos").value = evento.asientos || "";
		document.getElementById("movieHorario").value = evento.horarioSesion || "";
	} else if (evento.tipo === "concierto") {
		document.getElementById("artistName").value = evento.artista || "";
		document.getElementById("supportActs").value = evento.artistasApertura || "";
		document.getElementById("venueConcierto").value = evento.recinto || "";
		document.getElementById("capacityConcierto").value = evento.capacidad || "";
		document.getElementById("horaConcierto").value = evento.horaComienzo || "";
		document.getElementById("puertasConcierto").value = evento.aperturaPuertas || "";
		document.getElementById("parkingConcierto").checked = evento.parking || false;
	} else if (evento.tipo === "festival") {
		document.getElementById("festivalLineup").value = evento.cartelArtistas || "";
		document.getElementById("festivalDays").value = evento.diasDuracion || "";
		document.getElementById("festivalEndDate").value = evento.fechaFin || "";
		document.getElementById("venueFestival").value = evento.recinto || "";
		document.getElementById("capacityFestival").value = evento.capacidad || "";
		document.getElementById("horaFestival").value = evento.horaComienzo || "";
		document.getElementById("puertasFestival").value = evento.aperturaPuertas || "";
		document.getElementById("parkingFestival").checked = evento.parking || false;
	}

	// Tickets
	const ticketContainer = document.getElementById("ticketTypes");
	ticketContainer.innerHTML = "";

	if (evento.tickets && evento.tickets.length > 0) {
		evento.tickets.forEach(ticket => {
			const ticketHTML = document.createElement("div");
			ticketHTML.classList.add("ticket-type-item");
			ticketHTML.dataset.ticketId = ticket.id; // <- guardamos ID del ticket
			ticketHTML.innerHTML = `
                <div class="form-grid">
                    <div class="form-group">
                        <label>Tipo de Entrada *</label>
                        <input type="text" name="ticketTypeName[]" value="${ticket.tipo || ""}" required>
                    </div>
                    <div class="form-group">
                        <label>Precio (€) *</label>
                        <input type="number" name="ticketTypePrice[]" value="${ticket.precio || 0}" step="0.01" min="0" required>
                    </div>
                    <div class="form-group">
                        <label>Cantidad Disponible *</label>
                        <input type="number" name="ticketTypeQuantity[]" value="${ticket.cantidad || 0}" min="1" required>
                    </div>
                </div>
                <button type="button" class="btn-remove-ticket">🗑️ Eliminar</button>
            `;
			ticketContainer.appendChild(ticketHTML);
			ticketHTML.querySelector(".btn-remove-ticket").addEventListener("click", () => ticketHTML.remove());
		});
	}

	// Cambiar texto del botón según acción
	const submitBtn = document.getElementById("btnSubmitEvento");
	if (submitBtn) {
		if (evento.id) {
			submitBtn.textContent = "Guardar cambios";
			submitBtn.id = "btnEditarEvento"
		} else {
			submitBtn.textContent = "Publicar evento";
			submitBtn.id = "btnSubmitEvento";
		}
	}

}

/**
 * ---------- FUNCION MOSTRAR CAMPOS SEGUN TIPO ----------
 */
function mostrarCamposPorTipo(tipo) {
	const cineFields = document.querySelectorAll(".field-cine");
	const conciertoFields = document.querySelectorAll(".field-concierto");
	const festivalFields = document.querySelectorAll(".field-festival");

	cineFields.forEach(f => f.style.display = tipo === "cine" ? "block" : "none");
	conciertoFields.forEach(f => f.style.display = tipo === "concierto" ? "block" : "none");
	festivalFields.forEach(f => f.style.display = tipo === "festival" ? "block" : "none");
}

/**
 * ---------- AGREGAR NUEVO TICKET ----------
 */
function agregarTicket() {
	const ticketContainer = document.getElementById("ticketTypes");
	const ticketHTML = document.createElement("div");
	ticketHTML.classList.add("ticket-type-item");
	ticketHTML.innerHTML = `
        <div class="form-grid">
            <div class="form-group">
                <label>Tipo de Entrada *</label>
                <input type="text" name="ticketTypeName[]" value="" required>
            </div>
            <div class="form-group">
                <label>Precio (€) *</label>
                <input type="number" name="ticketTypePrice[]" value="0" step="0.01" min="0" required>
            </div>
            <div class="form-group">
                <label>Cantidad Disponible *</label>
                <input type="number" name="ticketTypeQuantity[]" value="1" min="1" required>
            </div>
        </div>
        <button type="button" class="btn-remove-ticket">🗑️ Eliminar</button>
    `;
	ticketContainer.appendChild(ticketHTML);
	ticketHTML.querySelector(".btn-remove-ticket").addEventListener("click", () => ticketHTML.remove());
}



/**
 * ---------- GUARDAR EDICIÓN DE EVENTO ----------
 */
async function editarEvento(eventoId) {
	if (!eventoId) {
		console.error("No hay evento cargado para editar.");
		return;
	}

	const form = document.getElementById("eventForm");
	const tipoInput = document.querySelector('input[name="eventType"]:checked');
	if (!tipoInput) {
		Swal.fire("Error", "Seleccione el tipo de evento", "warning");
		return;
	}
	const tipo = tipoInput.value.toLowerCase();

	// Crear FormData igual que para crear
	const formData = new FormData();
	formData.append("tipo", tipo);
	formData.append("nombre", document.getElementById("eventName").value);
	formData.append("descripcion", document.getElementById("eventDescription").value);
	formData.append("ciudad", document.getElementById("eventCity").value);
	formData.append("direccion", document.getElementById("eventAddress").value);
	formData.append("fecha", document.getElementById("eventDate").value);
	formData.append("contactoEmail", document.getElementById("eventContact").value);

	if (archivoImagen1) {
		formData.append("imagenFile", archivoImagen1);
	} else if (form.dataset.imagenUrl) {
		formData.append("imagenUrl", form.dataset.imagenUrl);
	}
	// Campos específicos según tipo
	if (tipo === "cine") {
		formData.append("tituloPelicula", document.getElementById("movieTitle").value);
		formData.append("director", document.getElementById("movieDirector").value);
		formData.append("clasificacion", document.getElementById("movieRating").value);
		formData.append("idioma", document.getElementById("movieLanguage").value);
		formData.append("sala", document.getElementById("movieSala").value);
		formData.append("asientos", document.getElementById("movieAsientos").value);
		formData.append("horarioSesionStr", document.getElementById("movieHorario").value);
	}
	if (tipo === "concierto") {
		formData.append("artista", document.getElementById("artistName").value);
		formData.append("artistasApertura", document.getElementById("supportActs").value);
		formData.append("recinto", document.getElementById("venueConcierto").value);
		formData.append("capacidad", document.getElementById("capacityConcierto").value);
		formData.append("horaComienzoStr", document.getElementById("horaConcierto").value);
		formData.append("aperturaPuertasStr", document.getElementById("puertasConcierto").value);
		formData.append("parking", document.getElementById("parkingConcierto").checked);
	}
	if (tipo === "festival") {
		formData.append("cartelArtistas", document.getElementById("festivalLineup").value);
		formData.append("diasDuracion", document.getElementById("festivalDays").value);
		formData.append("fechaFinStr", document.getElementById("festivalEndDate").value);
		formData.append("recinto", document.getElementById("venueFestival").value);
		formData.append("capacidad", document.getElementById("capacityFestival").value);
		formData.append("horaComienzoStr", document.getElementById("horaFestival").value);
		formData.append("aperturaPuertasStr", document.getElementById("puertasFestival").value);
		formData.append("parking", document.getElementById("parkingFestival").checked);
	}

	// Tickets
	document.querySelectorAll(".ticket-type-item").forEach(ticket => {
		formData.append("ticketsNombre", ticket.querySelector('input[name="ticketTypeName[]"]').value);
		formData.append("ticketsPrecio", ticket.querySelector('input[name="ticketTypePrice[]"]').value);
		formData.append("ticketsCantidad", ticket.querySelector('input[name="ticketTypeQuantity[]"]').value);
	});

	// URL de edición usando POST
	const url = `/eventos/${tipo}/editar/${eventoId}`;

	try {
		const res = await fetch(url, {
			method: "POST",  // POST para evitar problemas con multipart/form-data
			body: formData
		});

		if (!res.ok) throw new Error(await res.text());

		await res.json();

		Swal.fire({
			icon: "success",
			title: "Evento actualizado",
			timer: 1500,
			showConfirmButton: false
		});

		// Limpiar formulario
		limpiarFormularioEvento();

		// Recargar lista
		cargarEventos();

		// Volver a pestaña de eventos
		document.querySelector('.menu-item[data-tab="tab-lista-eventos"]').click();

	} catch (err) {
		console.error(err);
		Swal.fire("Error", err.message, "error");
	}
}

function limpiarFormularioEvento() {
    const form = document.getElementById("eventForm");

    // Reset nativo del formulario
    form.reset();

    // Eliminar ID de edición
    delete form.dataset.eventoid;

    // Vaciar tickets dinámicos
    const ticketContainer = document.getElementById("ticketTypes");
    if (ticketContainer) ticketContainer.innerHTML = "";

    // Limpiar previsualización y archivo temporal
    archivoImagen1 = null;
    const imagePreview = document.getElementById("imagePreview");
    if (imagePreview) imagePreview.src = "/images/default-image.jpg";

    // Reset campos por tipo (oculta todos)
    mostrarCamposPorTipo("");

    // Deseleccionar tipo de evento
    document.querySelectorAll('input[name="eventType"]').forEach(r => r.checked = false);
}


/**
 * ---------- SUBMIT DEL FORMULARIO ----------
 */
function llamarEvento() {
	document.getElementById("eventForm").addEventListener("submit", async function(e) {
		e.preventDefault(); // evita recargar la página

		const form = e.currentTarget;
		const eventoId = form.dataset.eventoid; // si existe, estamos editando

		console.log("ID", eventoId)

		if (eventoId) {
			await editarEvento(eventoId); // PUT
		} else {
			await crearEvento(); // POST
		}
	});
}




