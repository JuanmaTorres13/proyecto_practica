package com.eventzone.eventzone.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.service.EventoService;
import com.eventzone.eventzone.service.ImagenService;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ImagenService imagenService;  
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Evento>> getAllEvents() {
        return ResponseEntity.ok(eventoService.getAllEvents());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        return eventoService.getEventbyId(id)
                .map(e -> {
                    eventoService.deleteEvent(id);
                    logger.info("Evento eliminado con ID={}", id);
                    return ResponseEntity.ok("Evento eliminado correctamente");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Evento no encontrado"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        Optional<Evento> eventoOpt = eventoService.getEventbyId(id);
        if (eventoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Evento no encontrado");
        }
        return ResponseEntity.ok(eventoOpt.get());
    }


}
