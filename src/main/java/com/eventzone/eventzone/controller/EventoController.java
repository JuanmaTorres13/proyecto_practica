package com.eventzone.eventzone.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.EventoCine;
import com.eventzone.eventzone.model.EventoConcierto;
import com.eventzone.eventzone.model.EventoFestival;
import com.eventzone.eventzone.service.EventoService;

@Controller
@RequestMapping("/eventos")
public class EventoController {

	private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

	private final EventoService eventoService;

	public EventoController(EventoService eventoService) {
		this.eventoService = eventoService;
	}

	@GetMapping("/crear")
	public String mostrarCrearEvento() {
		return "eventos/create_event";
	}

	@GetMapping("/listar")
	public String mostrarListasEventos() {
		return "eventos/lista";
	}

	@GetMapping
	public ResponseEntity<List<Evento>> getAllEvents() {
		return ResponseEntity.ok(eventoService.getAllEvents());
	}

//	@GetMapping("/{id}")
//	public ResponseEntity<?> getEventbyId(@PathVariable Long id) {
//		return eventoService.getEventbyId(id).map(ResponseEntity::ok)
//				.orElse(ResponseEntity.status(404).body("Evento no encontrado"));
//	}

	@PostMapping("/crear")
	public ResponseEntity<?> createEvent(@RequestBody Evento evento) {
		try {
			Evento nuevo = eventoService.saveEvent(evento);
			return ResponseEntity.ok(nuevo);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al crear el evento");
		}
	}

	@PutMapping("/modificar/{id}")
	public ResponseEntity<?> updateEvento(@PathVariable Long id, @RequestBody Evento eventoActualizado) {

		return eventoService.getEventbyId(id).map(eventoExistente -> {

			eventoExistente.setNombre(eventoActualizado.getNombre());
			eventoExistente.setTipo(eventoActualizado.getTipo());
			eventoExistente.setCiudad(eventoActualizado.getCiudad());
			eventoExistente.setDireccion(eventoActualizado.getDireccion());
			eventoExistente.setFecha(eventoActualizado.getFecha());
			eventoExistente.setDescripcion(eventoActualizado.getDescripcion());
			eventoExistente.setImagenUrl(eventoActualizado.getImagenUrl());
			eventoExistente.setContactoEmail(eventoActualizado.getContactoEmail());
			
			eventoService.saveEvent(eventoExistente);

			return ResponseEntity.ok("Evento actualizado");
		}).orElse(ResponseEntity.status(404).body("Evento no encontrado"));
	}

	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable Long id, @RequestBody Evento evento) {
		return eventoService.getEventbyId(id).map(e -> {
			eventoService.deleteEvent(id);
			return ResponseEntity.ok("Evento eliminado correctamente");
		}).orElse(ResponseEntity.status(404).body("Evento no encontado"));
	}

	@GetMapping("/cine")
	public ResponseEntity<List<EventoCine>> getAllCines() {
		return ResponseEntity.ok(eventoService.getAllCines());
	}

	@PostMapping("/cine")
	public ResponseEntity<?> createCine(@RequestBody EventoCine cine) {
		try {
			EventoCine nuevo = eventoService.saveCine(cine);
			return ResponseEntity.ok(nuevo);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al crear el evento de cine");
		}
	}

	@GetMapping("/concierto")
	public ResponseEntity<List<EventoConcierto>> getAllConciertos() {
		return ResponseEntity.ok(eventoService.getAllConciertos());
	}

	@PostMapping("/concierto")
	public ResponseEntity<?> createConcierto(@RequestBody EventoConcierto concierto) {
		try {
			EventoConcierto nuevo = eventoService.saveConcierto(concierto);
			return ResponseEntity.ok(nuevo);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al crear el evento de concierto");
		}
	}

	@GetMapping("/festival")
	public ResponseEntity<List<EventoFestival>> getAllFestivales() {
		return ResponseEntity.ok(eventoService.getAllFestivales());
	}

	@PostMapping("/festival")
	public ResponseEntity<?> createFestival(@RequestBody EventoFestival festival) {
		try {
			EventoFestival nuevo = eventoService.saveFestival(festival);
			return ResponseEntity.ok(nuevo);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al crear el evento de festival");
		}
	}

}