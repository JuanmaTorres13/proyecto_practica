package com.eventzone.eventzone.controller;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.EventoFestival;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.EventoService;
import com.eventzone.eventzone.service.ImagenService;

@RestController
@RequestMapping("/eventos/festival")
public class FestivalController {

	private static final Logger logger = LoggerFactory.getLogger(FestivalController.class);

	@Autowired
	private EventoService eventoService;

	@Autowired
	private ImagenService imagenService;

	private void actualizarTickets(Evento evento, List<Ticket> tickets) {
		if (tickets == null)
			return;

		// Eliminar tickets que ya no están
		evento.getTickets()
				.removeIf(t -> tickets.stream().noneMatch(nt -> nt.getId() != null && nt.getId().equals(t.getId())));

		for (Ticket t : tickets) {
			if (t.getId() != null) {
				// Actualizar ticket existente
				evento.getTickets().stream().filter(existing -> existing.getId().equals(t.getId())).findFirst()
						.ifPresent(existing -> {
							existing.setTipo(t.getTipo());
							existing.setPrecio(t.getPrecio());
							existing.setCantidad(t.getCantidad());
						});
			} else {
				// Nuevo ticket
				evento.addTicket(t);
			}
		}
	}

	@PostMapping("/crear")
	public ResponseEntity<?> createFestival(@RequestParam("tipo") String tipo, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail, @RequestParam("imagenFile") MultipartFile imagen,
			@RequestParam(required = false) String cartelArtistas, @RequestParam(required = false) Integer diasDuracion,
			@RequestParam(required = false) String fechaFinStr, @RequestParam(required = false) String recinto,
			@RequestParam(required = false) Integer capacidad, @RequestParam(required = false) String horaComienzoStr,
			@RequestParam(required = false) String aperturaPuertasStr, @RequestParam(required = false) Boolean parking,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {
		try {
			String imagesDir = imagenService.guardarImagen(imagen);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss][.SSS]");
			LocalDate fecha = LocalDate.parse(fechaStr, formatter);
			LocalDate fechaFin = LocalDate.parse(fechaFinStr, formatter);

			EventoFestival festival = new EventoFestival();
			festival.setTipo(tipo);
			festival.setNombre(nombre);
			festival.setDescripcion(descripcion);
			festival.setCiudad(ciudad);
			festival.setDireccion(direccion);
			festival.setFecha(fecha);
			festival.setFechaFin(fechaFin);
			festival.setContactoEmail(contactoEmail);
			festival.setImagenUrl(imagesDir);
			festival.setCartelArtistas(cartelArtistas);
			festival.setFestivalDias(diasDuracion != null ? diasDuracion : 0);
			festival.setRecinto(recinto);
			festival.setCapacidad(capacidad != null ? capacidad : 0);
			festival.setHora(horaComienzoStr != null ? LocalTime.parse(horaComienzoStr, timeFormatter) : null);
			festival.setAperturaPuertas(
					aperturaPuertasStr != null ? LocalTime.parse(aperturaPuertasStr, timeFormatter) : null);
			festival.setParking(parking != null ? parking : false);

			// Crear tickets y asociarlos
			if (ticketsNombre != null && ticketsPrecio != null && ticketsCantidad != null) {
				for (int i = 0; i < ticketsNombre.size(); i++) {
					Ticket ticket = new Ticket();
					ticket.setTipo(ticketsNombre.get(i));
					ticket.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					ticket.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					festival.addTicket(ticket);
				}
			}

			EventoFestival nuevo = eventoService.saveFestival(festival);
			logger.info("Evento de festival creado: {}", nuevo.getNombre());
			return ResponseEntity.ok(nuevo);

		} catch (IOException e) {
			logger.error("Error al guardar la imagen", e);
			return ResponseEntity.status(500).body("Error al guardar la imagen");
		} catch (Exception e) {
			logger.error("Error creando evento de festival", e);
			return ResponseEntity.status(500).body("Error al crear el evento de festival");
		}
	}

	@GetMapping
	public ResponseEntity<List<EventoFestival>> getAllFestivales() {
		return ResponseEntity.ok(eventoService.getAllFestivales());
	}

	@PostMapping("/editar/{id}")
	public ResponseEntity<?> updateFestival(@PathVariable Long id, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail,
			@RequestParam(value = "imagenFile", required = false) MultipartFile imagen,
			@RequestParam(required = false) String cartelArtistas, @RequestParam(required = false) Integer diasDuracion,
			@RequestParam(required = false) String FechaFinStr, @RequestParam(required = false) String recinto,
			@RequestParam(required = false) Integer capacidad, @RequestParam(required = false) String horaComienzoStr,
			@RequestParam(required = false) String aperturaPuertasStr, @RequestParam(required = false) Boolean parking,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {

		try {
			EventoFestival festival = (EventoFestival) eventoService.getEventbyId(id).orElse(null);
			if (festival == null)
				return ResponseEntity.status(404).body("Evento no encontrado");

			//ACTUALIZAR CAMPOS
			festival.setNombre(nombre);
			festival.setDescripcion(descripcion);
			festival.setCiudad(ciudad);
			festival.setDireccion(direccion);
			festival.setFecha(LocalDate.parse(fechaStr));
			festival.setContactoEmail(contactoEmail);
			
			festival.setCartelArtistas(cartelArtistas);
			festival.setFestivalDias(diasDuracion);
			festival.setFechaFin(LocalDate.parse(FechaFinStr));
			festival.setRecinto(recinto);
			festival.setCapacidad(capacidad);
			festival.setHora(LocalTime.parse(horaComienzoStr));
			festival.setAperturaPuertas(LocalTime.parse(aperturaPuertasStr));
			festival.setParking(parking);

			//IMAGEN
	        if (imagen != null && !imagen.isEmpty()) {

	            String imagenAnterior = festival.getImagenUrl();  // URL antigua

	            String nuevaImagenUrl = imagenService.guardarImagen(imagen); // URL nueva

	            if (imagenAnterior != null && !imagenAnterior.isEmpty()) {
	                imagenService.eliminarImagen(imagenAnterior); // borrar anterior
	            }

	            festival.setImagenUrl(nuevaImagenUrl);
	        }
		
			//TICKETS
			festival.getTickets().clear();
			if(ticketsNombre != null) {
				for(int i = 0; i<ticketsNombre.size(); i++) {
					Ticket t = new Ticket();
					t.setTipo(ticketsNombre.get(i));
					t.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					t.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					t.setEvento(festival);
					festival.addTicket(t);
				}
			}

			EventoFestival actualizado = eventoService.saveFestival(festival);
			return ResponseEntity.ok(actualizado);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al editar el evento");
		}
	}
}
