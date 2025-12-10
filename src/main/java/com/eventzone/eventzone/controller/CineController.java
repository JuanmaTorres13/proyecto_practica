package com.eventzone.eventzone.controller;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.EventoCine;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.EventoService;
import com.eventzone.eventzone.service.ImagenService;

@RestController
@RequestMapping("/eventos/cine")
public class CineController {

	private static final Logger logger = LoggerFactory.getLogger(CineController.class);

	@Autowired
	private EventoService eventoService;

	@Autowired
	private ImagenService imagenService;

	@PostMapping("/crear")
	public ResponseEntity<?> createCine(@RequestParam("tipo") String tipo, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail, @RequestParam("imagenFile") MultipartFile imagen,
			@RequestParam(required = false) String tituloPelicula, @RequestParam(required = false) String director,
			@RequestParam(required = false) String clasificacion, @RequestParam(required = false) String idioma,
			@RequestParam(required = false) String sala, @RequestParam(required = false) Integer asientos,
			@RequestParam(required = false) String horarioSesionStr,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {
		try {
			String imagesDir = imagenService.guardarImagen(imagen);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss][.SSS]");
			LocalDate fecha = LocalDate.parse(fechaStr, formatter);

			EventoCine cine = new EventoCine();
			cine.setTipo(tipo);
			cine.setNombre(nombre);
			cine.setDescripcion(descripcion);
			cine.setCiudad(ciudad);
			cine.setDireccion(direccion);
			cine.setFecha(fecha);
			cine.setContactoEmail(contactoEmail);
			cine.setImagenUrl(imagesDir);
			cine.setCineTitulo(tituloPelicula);
			cine.setCineDirector(director);
			cine.setClasificacion(clasificacion);
			cine.setIdioma(idioma);
			cine.setCineSala(sala);
			cine.setCineAsientos(asientos);
			cine.setCineHorarios(horarioSesionStr != null ? LocalTime.parse(horarioSesionStr, timeFormatter) : null);

			// Crear tickets y asociarlos
			if (ticketsNombre != null && ticketsPrecio != null && ticketsCantidad != null) {
				for (int i = 0; i < ticketsNombre.size(); i++) {
					Ticket ticket = new Ticket();
					ticket.setTipo(ticketsNombre.get(i));
					ticket.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					ticket.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					cine.addTicket(ticket);
				}
			}

			EventoCine nuevo = eventoService.saveCine(cine);
			logger.info("Evento de cine creado: {}", nuevo.getNombre());
			return ResponseEntity.ok(nuevo);

		} catch (IOException e) {
			logger.error("Error al guardar la imagen", e);
			return ResponseEntity.status(500).body("Error al guardar la imagen");
		} catch (Exception e) {
			logger.error("Error creando evento de cine", e);
			return ResponseEntity.status(500).body("Error al crear el evento de cine");
		}
	}

	@GetMapping
	public ResponseEntity<List<EventoCine>> getAllCines() {
		return ResponseEntity.ok(eventoService.getAllCines());
	}

	@PostMapping("/editar/{id}")
	public ResponseEntity<?> updateCine(@PathVariable Long id, @RequestParam("nombre") String nombre,
			@RequestParam("descripcion") String descripcion, @RequestParam("ciudad") String ciudad,
			@RequestParam("direccion") String direccion, @RequestParam("fecha") String fechaStr,
			@RequestParam("contactoEmail") String contactoEmail,
			@RequestParam(value = "imagenFile", required = false) MultipartFile imagen,
			@RequestParam(required = false) String tituloPelicula, @RequestParam(required = false) String director,
			@RequestParam(required = false) String clasificacion, @RequestParam(required = false) String idioma,
			@RequestParam(required = false) String sala, @RequestParam(required = false) Integer asientos,
			@RequestParam(required = false) String horarioSesionStr,
			@RequestParam(required = false) List<String> ticketsNombre,
			@RequestParam(required = false) List<String> ticketsPrecio,
			@RequestParam(required = false) List<String> ticketsCantidad) {
		try {
			EventoCine cine = (EventoCine) eventoService.getEventbyId(id).orElse(null);
			if (cine == null) {
				return ResponseEntity.status(404).body("Evento no encontrado");
			}

			// Actualizar campos
			cine.setNombre(nombre);
			cine.setDescripcion(descripcion);
			cine.setCiudad(ciudad);
			cine.setDireccion(direccion);
			cine.setFecha(LocalDate.parse(fechaStr));
			cine.setContactoEmail(contactoEmail);

			cine.setCineTitulo(tituloPelicula);
			cine.setCineDirector(director);
			cine.setClasificacion(clasificacion);
			cine.setIdioma(idioma);
			cine.setCineSala(sala);
			cine.setCineAsientos(asientos);
			cine.setCineHorarios(horarioSesionStr != null ? LocalTime.parse(horarioSesionStr) : null);

			// IMAGEN
	        if (imagen != null && !imagen.isEmpty()) {

	            String imagenAnterior = cine.getImagenUrl();  // URL antigua

	            String nuevaImagenUrl = imagenService.guardarImagen(imagen); // URL nueva

	            if (imagenAnterior != null && !imagenAnterior.isEmpty()) {
	                imagenService.eliminarImagen(imagenAnterior); // borrar anterior
	            }

	            cine.setImagenUrl(nuevaImagenUrl);
	        }

			// TICKETS
			cine.getTickets().clear();
			if (ticketsNombre != null) {
				for (int i = 0; i < ticketsNombre.size(); i++) {
					Ticket t = new Ticket();
					t.setTipo(ticketsNombre.get(i));
					t.setPrecio(Double.parseDouble(ticketsPrecio.get(i)));
					t.setCantidad(Integer.parseInt(ticketsCantidad.get(i)));
					t.setEvento(cine);
					cine.addTicket(t);
				}
			}

			EventoCine actualizado = eventoService.saveCine(cine);
			return ResponseEntity.ok(actualizado);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Error al editar el evento");
		}
	}

}
