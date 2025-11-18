document.addEventListener("DOMContentLoaded", function () {
    const eventForm = document.getElementById("eventForm");
    const eventTypeRadios = document.querySelectorAll('input[name="eventType"]');

    const cineFields = document.querySelectorAll(".field-cine");
    const conciertoFields = document.querySelectorAll(".field-concierto");
    const festivalFields = document.querySelectorAll(".field-festival");

    // Mostrar/ocultar campos según tipo
    eventTypeRadios.forEach(radio => {
        radio.addEventListener("change", function () {
            const tipo = this.value;
            cineFields.forEach(f => f.style.display = "none");
            conciertoFields.forEach(f => f.style.display = "none");
            festivalFields.forEach(f => f.style.display = "none");

            if (tipo === "cine") cineFields.forEach(f => f.style.display = "block");
            if (tipo === "concierto") conciertoFields.forEach(f => f.style.display = "block");
            if (tipo === "festival") festivalFields.forEach(f => f.style.display = "block");
        });
    });

    // Tickets dinámicos
    const addTicketBtn = document.getElementById("addTicketBtn");
    const ticketTypesContainer = document.getElementById("ticketTypes");

    addTicketBtn.addEventListener("click", function () {
        const newTicket = ticketTypesContainer.firstElementChild.cloneNode(true);
        newTicket.querySelectorAll("input").forEach(input => input.value = "");
        const removeBtn = newTicket.querySelector(".btn-remove-ticket");
        removeBtn.style.display = "inline-block";
        removeBtn.addEventListener("click", function () {
            newTicket.remove();
        });
        ticketTypesContainer.appendChild(newTicket);
    });

    // Enviar datos al backend
    eventForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const tipoEvento = document.querySelector('input[name="eventType"]:checked')?.value;
        if (!tipoEvento) {
            alert("Selecciona un tipo de evento");
            return;
        }

        // Construir objeto base
        const eventData = {
            tipo: tipoEvento,
            nombre: document.getElementById("eventName").value,
            descripcion: document.getElementById("eventDescription").value,
            ciudad: document.getElementById("eventCity").value,
            direccion: document.getElementById("eventAddress").value,
            fecha: document.getElementById("eventDate").value,
            imagenUrl: document.getElementById("imagenUrl").file[0],
            contactoEmail: document.getElementById("eventContact").value,
            tickets: Array.from(ticketTypesContainer.children).map(ticket => ({
                nombre: ticket.querySelector('input[name="ticketTypeName[]"]').value,
                precio: parseFloat(ticket.querySelector('input[name="ticketTypePrice[]"]').value),
                cantidad: parseInt(ticket.querySelector('input[name="ticketTypeQuantity[]"]').value)
            }))
        };

        // Campos específicos
        if (tipoEvento === "cine") {a
            eventData.tituloPelicula = document.getElementById("movieTitle").value;
            eventData.director = document.getElementById("movieDirector").value;
            eventData.clasificacion = document.getElementById("movieRating").value;
            eventData.idioma = document.getElementById("movieLanguage").value;
        }

        if (tipoEvento === "concierto") {
            eventData.artista = document.getElementById("artistName").value;
            eventData.artistasApertura = document.getElementById("supportActs").value;
            eventData.recinto = document.getElementById("eventVenue").value;
            eventData.capacidad = parseInt(document.getElementById("eventCapacity").value) || 0;
            eventData.horaComienzo = document.getElementById("eventTime").value;
            eventData.aperturaPuertas = document.getElementById("eventDoors").value;
            eventData.parking = document.getElementById("eventParking").checked;
        }

        if (tipoEvento === "festival") {
            eventData.cartelArtistas = document.getElementById("festivalLineup").value;
            eventData.diasDuracion = parseInt(document.getElementById("festivalDays").value) || 0;
            eventData.fechaFin = document.getElementById("eventEndDate").value;
            eventData.recinto = document.getElementById("eventVenue").value;
            eventData.capacidad = parseInt(document.getElementById("eventCapacity").value) || 0;
            eventData.horaComienzo = document.getElementById("eventTime").value;
            eventData.aperturaPuertas = document.getElementById("eventDoors").value;
            eventData.parking = document.getElementById("eventParking").checked;
        }

        // Enviar via fetch al backend (Spring Boot REST)
        fetch("/eventos/crear", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(eventData)
        })
        .then(response => {
            if (!response.ok) throw new Error("Error al crear el evento");
            return response.json();
        })
        .then(data => {
            Swal.fire({
                icon: 'success',
                title: 'Evento creado',
                text: '¡El evento se ha publicado correctamente!'
            });
            eventForm.reset();
            cineFields.forEach(f => f.style.display = "none");
            conciertoFields.forEach(f => f.style.display = "none");
            festivalFields.forEach(f => f.style.display = "none");
            while (ticketTypesContainer.children.length > 1) ticketTypesContainer.lastChild.remove();
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'No se pudo crear el evento. ' + error.message
            });
        });
    });
});
