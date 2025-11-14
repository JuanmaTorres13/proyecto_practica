document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("eventForm");
    const ticketContainer = document.getElementById("ticketTypes");
    const addTicketBtn = document.getElementById("addTicketBtn");

    // =========================
    // Cambiar campos según tipo de evento
    // =========================
    document.querySelectorAll('input[name="eventType"]').forEach(radio => {
        radio.addEventListener("change", mostrarCamposPorTipo);
    });

    function mostrarCamposPorTipo() {
        const tipo = document.querySelector('input[name="eventType"]:checked').value;

        document.querySelectorAll(".field-cine").forEach(el => el.style.display = tipo === "cine" ? "block" : "none");
        document.querySelectorAll(".field-concierto").forEach(el => el.style.display = tipo === "concierto" ? "block" : "none");
        document.querySelectorAll(".field-festival").forEach(el => el.style.display = tipo === "festival" ? "block" : "none");

        // Mostrar opciones de género según tipo
        document.querySelectorAll("#eventGenre option").forEach(opt => {
            opt.style.display = (opt.classList.contains(`genre-${tipo}`)) ? "block" : "none";
        });
    }

    // =========================
    // Añadir/Eliminar tickets dinámicamente
    // =========================
    addTicketBtn.addEventListener("click", () => {
        const newTicket = ticketContainer.querySelector(".ticket-type-item").cloneNode(true);
        newTicket.querySelectorAll("input").forEach(input => input.value = "");
        newTicket.querySelector(".btn-remove-ticket").style.display = "inline-block";
        ticketContainer.appendChild(newTicket);

        // Añadir evento al botón eliminar
        newTicket.querySelector(".btn-remove-ticket").addEventListener("click", () => {
            newTicket.remove();
        });
    });

    ticketContainer.querySelectorAll(".btn-remove-ticket").forEach(btn => {
        btn.addEventListener("click", e => {
            e.target.closest(".ticket-type-item").remove();
        });
    });

    // =========================
    // Capturar submit del formulario
    // =========================
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const eventoData = recolectarDatosEvento();
        enviarEvento(eventoData);
    });

    // =========================
    // Construir objeto Evento
    // =========================
    function recolectarDatosEvento() {
        const tipo = document.querySelector('input[name="eventType"]:checked')?.value;
        const tickets = [];

        document.querySelectorAll("#ticketTypes .ticket-type-item").forEach(item => {
            tickets.push({
                tipo: item.querySelector('input[name="ticketTypeName[]"]').value,
                precio: parseFloat(item.querySelector('input[name="ticketTypePrice[]"]').value),
                cantidad: parseInt(item.querySelector('input[name="ticketTypeQuantity[]"]').value),
                descripcion: item.querySelector('input[name="ticketTypeDescription[]"]').value
            });
        });

        return {
            tipo: tipo,
            nombre: document.getElementById("eventName").value,
            descripcion: document.getElementById("eventDescription").value,
            genero: document.getElementById("eventGenre").value,
            imagenUrl: document.getElementById("eventImage").value,
            duracion: document.getElementById("eventDuration").value,
            fecha: document.getElementById("eventDate").value,
            fechaFin: document.getElementById("eventEndDate")?.value || null,
            hora: document.getElementById("eventTime")?.value || null,
            aperturaPuertas: document.getElementById("eventDoors")?.value || null,
            ciudad: document.getElementById("eventCity").value,
            direccion: document.getElementById("eventAddress").value,
            capacidad: parseInt(document.getElementById("eventCapacity")?.value) || null,
            contactoEmail: document.getElementById("eventContact").value,
            restriccionesEdad: document.getElementById("eventRestrictions").value,
            normas: document.getElementById("eventRules").value,
            parking: document.getElementById("eventParking").checked,
            accesible: document.getElementById("eventAccessible").checked,
            comida: document.getElementById("eventFood").checked,
            // Campos específicos por tipo
            cineTitulo: tipo === "cine" ? document.getElementById("movieTitle").value : null,
            cineDirector: tipo === "cine" ? document.getElementById("movieDirector").value : null,
            cineNombre: tipo === "cine" ? document.getElementById("cinemaName").value : null,
            cineSala: tipo === "cine" ? document.getElementById("cinemaRoom").value : null,
            cineAsientos: tipo === "cine" ? parseInt(document.getElementById("cinemaSeats").value) : null,
            cineHorarios: tipo === "cine" ? document.getElementById("cinemaShowtimes").value : null,
            cineIdioma: tipo === "cine" ? document.getElementById("movieLanguage")?.value : null,

            artista: tipo === "concierto" ? document.getElementById("artistName").value : null,
            artistasApertura: tipo === "concierto" ? document.getElementById("supportActs").value : null,
            recinto: (tipo === "concierto" || tipo === "festival") ? document.getElementById("eventVenue")?.value : null,

            festivalLineup: tipo === "festival" ? document.getElementById("festivalLineup")?.value : null,
            festivalDias: tipo === "festival" ? parseInt(document.getElementById("festivalDays")?.value) : null,

            tickets: tickets
        };
    }

    // =========================
    // Enviar evento con AJAX
    // =========================
    function enviarEvento(evento) {
        fetch("/eventos/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(evento)
        })
        .then(response => {
            if (!response.ok) throw new Error("Error al crear el evento");
            return response.json();
        })
        .then(data => {
            Swal.fire({
                icon: "success",
                title: "¡Evento creado!",
                text: "Tu evento se ha publicado correctamente."
            });
            form.reset();
        })
        .catch(error => {
            console.error(error);
            Swal.fire({
                icon: "error",
                title: "Oops...",
                text: "No se pudo crear el evento."
            });
        });
    }
});
