document.addEventListener("DOMContentLoaded", function() {
	console.log("JS cargado");

	const eventForm = document.getElementById("eventForm");
	const eventTypeRadios = document.querySelectorAll('input[name="eventType"]');

	const cineFields = document.querySelectorAll(".field-cine");
	const conciertoFields = document.querySelectorAll(".field-concierto");
	const festivalFields = document.querySelectorAll(".field-festival");

	const addTicketBtn = document.getElementById("addTicketBtn");
	const ticketTypesContainer = document.getElementById("ticketTypes");

	// Imagen
	const imagenInput = document.getElementById("imagenFile");
	let archivoImagen = null;

	if (imagenInput) {
		imagenInput.addEventListener("change", function() {
			archivoImagen = this.files[0];
			if (archivoImagen) {
				const validTypes = ["image/png", "image/jpeg", "image/jpg", "image/gif"];
				if (!validTypes.includes(archivoImagen.type)) {
					alert("Tipo de archivo no válido. Solo PNG, JPG o GIF.");
					archivoImagen = null;
					this.value = "";
				}
			}
		});
	} else {
		console.warn("No se encontró input de imagen con id 'imagenFile'");
	}

	// Mostrar/ocultar campos según tipo de evento
	eventTypeRadios.forEach(radio => {
		radio.addEventListener("change", function() {
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
	if (addTicketBtn && ticketTypesContainer) {
		addTicketBtn.addEventListener("click", function() {
			const firstTicket = ticketTypesContainer.firstElementChild;
			if (!firstTicket) return;
			const newTicket = firstTicket.cloneNode(true);
			newTicket.querySelectorAll("input").forEach(input => input.value = "");
			const removeBtn = newTicket.querySelector(".btn-remove-ticket");
			if (removeBtn) {
				removeBtn.style.display = "inline-block";
				removeBtn.addEventListener("click", function() {
					newTicket.remove();
				});
			}
			ticketTypesContainer.appendChild(newTicket);
		});
	}

	// Enviar formulario
	if (eventForm) {
		eventForm.addEventListener("submit", function(e) {
			e.preventDefault();

			const tipoEvento = document.querySelector('input[name="eventType"]:checked')?.value;
			if (!tipoEvento) {
				alert("Selecciona un tipo de evento");
				return;
			}

			if (!archivoImagen) {
				alert("Selecciona una imagen para el evento");
				return;
			}

			const formData = new FormData();
			formData.append("tipo", tipoEvento);
			formData.append("nombre", document.getElementById("eventName").value);
			formData.append("descripcion", document.getElementById("eventDescription").value);
			formData.append("ciudad", document.getElementById("eventCity").value);
			formData.append("direccion", document.getElementById("eventAddress").value);
			formData.append("fecha", document.getElementById("eventDate").value);
			formData.append("contactoEmail", document.getElementById("eventContact").value);
			formData.append("imagenFile", archivoImagen);

			// Tickets
			Array.from(ticketTypesContainer.children).forEach(ticket => {
				formData.append("ticketsNombre", ticket.querySelector('input[name="ticketTypeName[]"]').value);
				formData.append("ticketsPrecio", ticket.querySelector('input[name="ticketTypePrice[]"]').value);
				formData.append("ticketsCantidad", ticket.querySelector('input[name="ticketTypeQuantity[]"]').value);
			});

			// Campos según tipo
			let url = "";
			if (tipoEvento === "cine") {
				url = "/eventos/cine/crear";
				formData.append("tituloPelicula", document.getElementById("movieTitle").value);
				formData.append("director", document.getElementById("movieDirector").value);
				formData.append("clasificacion", document.getElementById("movieRating").value);
				formData.append("idioma", document.getElementById("movieLanguage").value);
			} else if (tipoEvento === "concierto") {
				url = "/eventos/concierto/crear";
				formData.append("artista", document.getElementById("artistName").value);
				formData.append("artistasApertura", document.getElementById("supportActs").value);
				formData.append("recinto", document.getElementById("venueConcierto").value);
				formData.append("capacidad", document.getElementById("capacityConcierto").value || 0);
				formData.append("horaComienzoStr", document.getElementById("horaConcierto").value);
				formData.append("aperturaPuertasStr", document.getElementById("puertasConcierto").value);
				formData.append("parking", document.getElementById("parkingConcierto").checked);
			} else if (tipoEvento === "festival") {
				url = "/eventos/festival/crear";
				formData.append("cartelArtistas", document.getElementById("festivalLineup").value);
				formData.append("diasDuracion", document.getElementById("festivalDays").value || 0);
				formData.append("fechaFinStr", document.getElementById("festivalEndDate").value);
				formData.append("recinto", document.getElementById("venueFestival").value);
				formData.append("capacidad", document.getElementById("capacityFestival").value || 0);
				formData.append("horaComienzoStr", document.getElementById("horaFestival").value);
				formData.append("aperturaPuertasStr", document.getElementById("puertasFestival").value);
				formData.append("parking", document.getElementById("parkingFestival").checked);
			}


			fetch(url, { method: "POST", body: formData })
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
					archivoImagen = null;
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
	}
});
