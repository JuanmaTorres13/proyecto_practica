document.addEventListener("DOMContentLoaded", () => {
    initTabs();
    initLogout();
	cargarUsuarios();
});

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

/**
 * ---------- CARGAR EVENTOS ----------
 */
async function cargarEventos() {
    const cont = document.getElementById("listaEventos");
    if (!cont) return;

    try {
        const res = await fetch("/eventos");
        if (!res.ok) throw new Error("No se pudieron obtener los eventos");
        const eventos = await res.json();

        if (eventos.length === 0) {
            cont.innerHTML = "<p class='empty-list'>No hay eventos creados.</p>";
            return;
        }

        cont.innerHTML = eventos.map(e => {
            // Determinar clase y texto del badge según tipo
            let badgeClass = "badge-default";
            let badgeText = e.tipo || "Evento";

            if (badgeText.toLowerCase() === "cine") badgeClass = "badge-cine";
            else if (badgeText.toLowerCase() === "concierto") badgeClass = "badge-concierto";
            else if (badgeText.toLowerCase() === "festival") badgeClass = "badge-festival";

            return `
            <div class="ticket-card" data-id="${e.id}">
                <div class="ticket-info">
                    <div class="ticket-avatar-section">
                        <img src="${e.imagenUrl || '/images/default-event.jpg'}" alt="${e.nombre}" class="ticket-avatar">
                    </div>
                    <div class="ticket-details">
                        <div class="ticket-header">
                            <h3>${e.nombre}</h3>
                            <span class="badge ${badgeClass}">${badgeText}</span>
                        </div>
                        <p>Fecha: ${e.fecha}${e.hora ? " " + e.hora : ""}</p>
                        <p>Ciudad: ${e.ciudad || ''}${e.direccion ? ", " + e.direccion : ''}</p>
                    </div>
                    <div class="ticket-actions">
                        <button class="btn-delete-user btn-edit">✏️ Editar</button>
                        <button class="btn-delete-user btn-delete">🗑️ Eliminar</button>
                    </div>
                </div>
            </div>`;
        }).join("");

        // Botones eliminar
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

        // Aquí puedes agregar funcionalidad de "Editar"
    } catch (err) {
        console.error(err);
        cont.innerHTML = "<p class='empty-list'>Error al cargar los eventos.</p>";
    }
}


/**
 * ---------- FUNCION LLENAR FORMULARIO ----------
 */
function llenarFormularioEvento(evento) {
    // Tipo de evento
    document.querySelectorAll('input[name="eventType"]').forEach(r => r.checked = false);
    if (evento.tipo) {
        const tipoInput = document.querySelector(`input[name="eventType"][value="${evento.tipo}"]`);
        if (tipoInput) tipoInput.checked = true;
    }

    // Campos comunes
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
