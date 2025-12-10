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
import com.eventzone.eventzone.model.EventoConcierto;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.EventoService;
import com.eventzone.eventzone.service.ImagenService;

@RestController
@RequestMapping("/eventos/concierto")
public class ConciertoController {

	private static final Logger logger = LoggerFactory.getLogger(ConciertoController.class);

	@Autowired
	private EventoService eventoService;

	@Autowired
	private ImagenService imagenService;

	@PostMapping("/crear")
	public ResponseEntity<?> createConcierto(@RequestParam("tipo") String tipo, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail, @RequestParam("imagenFile") MultipartFile imagen,
			@RequestParam(required = false) String artista, @RequestParam(required = false) String artistasApertura,
			@RequestParam(required = false) String recinto, @RequestParam(required = false) Integer capacidad,
			@RequestParam(required = false) String horaComienzoStr,
			@RequestParam(required = false) String aperturaPuertasStr, @RequestParam(required = false) Boolean parking,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {
		try {
			String imagesDir = imagenService.guardarImagen(imagen);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss][.SSS]");
			LocalDate fecha = LocalDate.parse(fechaStr, formatter);

			EventoConcierto concierto = new EventoConcierto();
			concierto.setTipo(tipo);
			concierto.setNombre(nombre);
			concierto.setDescripcion(descripcion);
			concierto.setCiudad(ciudad);
			concierto.setDireccion(direccion);
			concierto.setFecha(fecha);
			concierto.setContactoEmail(contactoEmail);
			concierto.setImagenUrl(imagesDir);
			concierto.setArtista(artista);
			concierto.setArtistasApertura(artistasApertura);
			concierto.setRecinto(recinto);
			concierto.setCapacidad(capacidad != null ? capacidad : 0);
			concierto.setHora(horaComienzoStr != null ? LocalTime.parse(horaComienzoStr, timeFormatter) : null);
			concierto.setAperturaPuertas(
					aperturaPuertasStr != null ? LocalTime.parse(aperturaPuertasStr, timeFormatter) : null);
			concierto.setParking(parking != null ? parking : false);

			// Crear tickets y asociarlos
			if (ticketsNombre != null && ticketsPrecio != null && ticketsCantidad != null) {
				for (int i = 0; i < ticketsNombre.size(); i++) {
					Ticket ticket = new Ticket();
					ticket.setTipo(ticketsNombre.get(i));
					ticket.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					ticket.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					concierto.addTicket(ticket);
				}
			}

			EventoConcierto nuevo = eventoService.saveConcierto(concierto);
			logger.info("Evento de concierto creado: {}", nuevo.getNombre());
			return ResponseEntity.ok(nuevo);

		} catch (IOException e) {
			logger.error("Error al guardar la imagen", e);
			return ResponseEntity.status(500).body("Error al guardar la imagen");

		} catch (Exception e) {
			logger.error("Error creando evento de concierto", e);
			return ResponseEntity.status(500).body("Error al crear el evento de concierto");
		}
	}

	@GetMapping
	public ResponseEntity<List<EventoConcierto>> getAllConciertos() {
		return ResponseEntity.ok(eventoService.getAllConciertos());
	}

	@PostMapping("/editar/{id}")
	public ResponseEntity<?> updateConcierto(@PathVariable Long id, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail,
			@RequestParam(value = "imagenFile", required = false) MultipartFile imagen,
			@RequestParam(required = false) String artista, @RequestParam(required = false) String artistasApertura,
			@RequestParam(required = false) String recinto, @RequestParam(required = false) Integer capacidad,
			@RequestParam(required = false) String horaComienzoStr,
			@RequestParam(required = false) String aperturaPuertasStr, @RequestParam(required = false) Boolean parking,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {
		try {
			EventoConcierto concierto = (EventoConcierto) eventoService.getEventbyId(id).orElse(null);
			if (concierto == null)
				return ResponseEntity.status(404).body("Evento no encontrado");

			concierto.setNombre(nombre);
			concierto.setDescripcion(descripcion);
			concierto.setCiudad(ciudad);
			concierto.setDireccion(direccion);
			concierto.setFecha(LocalDate.parse(fechaStr));
			concierto.setContactoEmail(contactoEmail);

			concierto.setArtista(artista);
			concierto.setArtistasApertura(artistasApertura);
			concierto.setRecinto(recinto);
			concierto.setCapacidad(capacidad);
			concierto.setHora(LocalTime.parse(horaComienzoStr));
			concierto.setAperturaPuertas(LocalTime.parse(aperturaPuertasStr));
			concierto.setParking(parking);

			// IMAGEN
			if (imagen != null && !imagen.isEmpty()) {

				String imagenAnterior = concierto.getImagenUrl(); // URL antigua

				String nuevaImagenUrl = imagenService.guardarImagen(imagen); // URL nueva

				if (imagenAnterior != null && !imagenAnterior.isEmpty()) {
					imagenService.eliminarImagen(imagenAnterior); // borrar anterior
				}

				concierto.setImagenUrl(nuevaImagenUrl);
			}

			// TICKETS
			concierto.getTickets().clear();
			if (ticketsNombre != null) {
				for (int i = 0; i < ticketsNombre.size(); i++) {
					Ticket t = new Ticket();
					t.setTipo(ticketsNombre.get(i));
					t.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					t.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					t.setEvento(concierto);
					concierto.addTicket(t);
				}
			}

			EventoConcierto actualizado = eventoService.saveConcierto(concierto);
			return ResponseEntity.ok(actualizado);

		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al editar el evento");
		}
	}
}
