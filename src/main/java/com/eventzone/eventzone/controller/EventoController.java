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
import com.eventzone.eventzone.model.EventoCine;
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
    
    @GetMapping
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

    
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> updateCine(
            @PathVariable Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("direccion") String direccion,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("contactoEmail") String contactoEmail,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagen,
            @RequestParam(required = false) String tituloPelicula,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String clasificacion,
            @RequestParam(required = false) String idioma
    ) {
        try {
            EventoCine cine = (EventoCine) eventoService.getEventbyId(id).orElse(null);
            if (cine == null) return ResponseEntity.status(404).body("Evento no encontrado");

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

            // Si cambia imagen
            if (imagen != null && !imagen.isEmpty()) {
                String imagesDir = "src/main/resources/static/images/";
                String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Path path = Paths.get(imagesDir + nombreArchivo);
                Files.copy(imagen.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                cine.setImagenUrl("/images/" + nombreArchivo);
            }

            EventoCine actualizado = eventoService.saveCine(cine);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al editar el evento");
        }
    }


}
