package com.eventzone.eventzone.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.EventoCine;
import com.eventzone.eventzone.model.EventoConcierto;
import com.eventzone.eventzone.model.EventoFestival;
import com.eventzone.eventzone.service.EventoService;

/**
 * Controlador encargado de manejar todas las operaciones CRUD relacionadas con los eventos.
 * <p>
 * Incluye manejo para los tipos de eventos:
 * <ul>
 *     <li>Eventos generales</li>
 *     <li>Eventos de cine</li>
 *     <li>Eventos de conciertos</li>
 *     <li>Eventos de festivales</li>
 * </ul>
 * </p>
 *
 * Los métodos devuelven {@link ResponseEntity} con códigos HTTP apropiados.
 */
@Controller
@RequestMapping("/eventos")
public class EventoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);

    private final EventoService eventoService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param eventoService servicio encargado de la lógica de negocio de los eventos
     */
    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // ============================================================
    //                           VISTAS
    // ============================================================

    /**
     * Muestra la vista del formulario para crear eventos.
     *
     * @return nombre de la plantilla Thymeleaf correspondiente
     */
    @GetMapping("/crear")
    public String mostrarCrearEvento() {
        return "eventos/create_event";
    }

    /**
     * Muestra la vista con el listado de eventos.
     *
     * @return nombre de la plantilla Thymeleaf correspondiente
     */
    @GetMapping("/listar")
    public String mostrarListasEventos() {
        return "eventos/lista";
    }

    // ============================================================
    //                      EVENTOS GENERALES
    // ============================================================

    /**
     * Obtiene la lista completa de eventos.
     *
     * @return lista de {@link Evento} dentro de un ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<Evento>> getAllEvents() {
        return ResponseEntity.ok(eventoService.getAllEvents());
    }

    /**
     * Crea un nuevo evento general en la base de datos.
     *
     * @param evento objeto {@link Evento} enviado en el cuerpo de la petición
     * @return evento creado o mensaje de error
     */
    @PostMapping("/crear")
    public ResponseEntity<?> createEvent(@RequestBody Evento evento) {
        try {
            Evento nuevo = eventoService.saveEvent(evento);
            logger.info("Evento creado correctamente: {}", nuevo.getNombre());
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            logger.error("Error al crear evento.", e);
            return ResponseEntity.status(500).body("Error al crear el evento");
        }
    }

    /**
     * Actualiza un evento existente según su ID.
     *
     * @param id identificador del evento a actualizar
     * @param eventoActualizado datos nuevos enviados en la petición
     * @return mensaje de éxito o error 404 si el evento no existe
     */
    @PutMapping("/modificar/{id}")
    public ResponseEntity<?> updateEvento(@PathVariable Long id, @RequestBody Evento eventoActualizado) {

        return eventoService.getEventbyId(id)
                .map(eventoExistente -> {

                    eventoExistente.setNombre(eventoActualizado.getNombre());
                    eventoExistente.setTipo(eventoActualizado.getTipo());
                    eventoExistente.setCiudad(eventoActualizado.getCiudad());
                    eventoExistente.setDireccion(eventoActualizado.getDireccion());
                    eventoExistente.setFecha(eventoActualizado.getFecha());
                    eventoExistente.setDescripcion(eventoActualizado.getDescripcion());
                    eventoExistente.setImagenUrl(eventoActualizado.getImagenUrl());
                    eventoExistente.setContactoEmail(eventoActualizado.getContactoEmail());

                    eventoService.saveEvent(eventoExistente);

                    logger.info("Evento actualizado: {}", eventoExistente.getNombre());
                    return ResponseEntity.ok("Evento actualizado");
                })
                .orElseGet(() -> {
                    logger.warn("Actualización fallida. Evento ID={} no encontrado", id);
                    return ResponseEntity.status(404).body("Evento no encontrado");
                });
    }

    /**
     * Elimina un evento según su ID.
     *
     * @param id identificador del evento a eliminar
     * @return mensaje de éxito o error si no existe
     */
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        return eventoService.getEventbyId(id)
                .map(e -> {
                    eventoService.deleteEvent(id);
                    logger.info("Evento eliminado con ID={}", id);
                    return ResponseEntity.ok("Evento eliminado correctamente");
                })
                .orElseGet(() -> {
                    logger.warn("Intento de eliminar evento inexistente. ID={}", id);
                    return ResponseEntity.status(404).body("Evento no encontrado");
                });
    }

    // ============================================================
    //                      EVENTOS DE CINE
    // ============================================================

    /**
     * Devuelve una lista de todos los eventos de tipo cine.
     *
     * @return lista de {@link EventoCine}
     */
    @GetMapping("/cine")
    public ResponseEntity<List<EventoCine>> getAllCines() {
        return ResponseEntity.ok(eventoService.getAllCines());
    }

    /**
     * Crea un nuevo evento de tipo Cine.
     *
     * @param cine objeto enviado en el cuerpo de la petición
     * @return evento creado o error 500
     */
    @PostMapping("/cine")
    public ResponseEntity<?> createCine(@RequestBody EventoCine cine) {
        try {
            EventoCine nuevo = eventoService.saveCine(cine);
            logger.info("Evento de cine creado correctamente");
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            logger.error("Error creando evento de cine", e);
            return ResponseEntity.status(500).body("Error al crear el evento de cine");
        }
    }

    // ============================================================
    //                    EVENTOS DE CONCIERTO
    // ============================================================

    /**
     * Obtiene la lista de eventos de conciertos.
     *
     * @return lista de {@link EventoConcierto}
     */
    @GetMapping("/concierto")
    public ResponseEntity<List<EventoConcierto>> getAllConciertos() {
        return ResponseEntity.ok(eventoService.getAllConciertos());
    }

    /**
     * Crea un evento de concierto.
     *
     * @param concierto objeto enviado en la petición
     * @return evento creado o error 500
     */
    @PostMapping("/concierto")
    public ResponseEntity<?> createConcierto(@RequestBody EventoConcierto concierto) {
        try {
            EventoConcierto nuevo = eventoService.saveConcierto(concierto);
            logger.info("Evento de concierto creado correctamente");
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            logger.error("Error creando evento de concierto", e);
            return ResponseEntity.status(500).body("Error al crear el evento de concierto");
        }
    }

    // ============================================================
    //                    EVENTOS DE FESTIVAL
    // ============================================================

    /**
     * Obtiene la lista de eventos de festival.
     *
     * @return lista de {@link EventoFestival}
     */
    @GetMapping("/festival")
    public ResponseEntity<List<EventoFestival>> getAllFestivales() {
        return ResponseEntity.ok(eventoService.getAllFestivales());
    }

    /**
     * Crea un evento de tipo festival.
     *
     * @param festival datos enviados en la petición
     * @return evento creado o error 500
     */
    @PostMapping("/festival")
    public ResponseEntity<?> createFestival(@RequestBody EventoFestival festival) {
        try {
            EventoFestival nuevo = eventoService.saveFestival(festival);
            logger.info("Evento de festival creado correctamente");
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            logger.error("Error creando evento de festival", e);
            return ResponseEntity.status(500).body("Error al crear el evento de festival");
        }
    }
}
