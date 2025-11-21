package com.eventzone.eventzone.controller;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eventzone.eventzone.model.EventoConcierto;
import com.eventzone.eventzone.service.EventoService;

@RestController
@RequestMapping("/eventos/concierto")
public class ConciertoController {

    private final EventoService eventoService;
    private static final Logger logger = LoggerFactory.getLogger(ConciertoController.class);

    public ConciertoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> createConcierto(
    		@RequestParam("tipo") String tipo,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("direccion") String direccion,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("contactoEmail") String contactoEmail,
            @RequestParam("imagenFile") MultipartFile imagen,
            @RequestParam(required = false) String artista,
            @RequestParam(required = false) String artistasApertura,
            @RequestParam(required = false) String recinto,
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(required = false) String horaComienzoStr,
            @RequestParam(required = false) String aperturaPuertasStr,
            @RequestParam(required = false) Boolean parking
    ) {
        try {
            // Guardar imagen
            String imagesDir = "src/main/resources/static/images/";
            String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            Path path = Paths.get(imagesDir + nombreArchivo);
            Files.createDirectories(path.getParent());
            Files.copy(imagen.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime horaComienzo = (horaComienzoStr != null) ? LocalTime.parse(horaComienzoStr) : null;
            LocalTime aperturaPuertas = (aperturaPuertasStr != null) ? LocalTime.parse(aperturaPuertasStr) : null;

            EventoConcierto concierto = new EventoConcierto();
            concierto.setTipo(tipo);
            concierto.setNombre(nombre);
            concierto.setDescripcion(descripcion);
            concierto.setCiudad(ciudad);
            concierto.setDireccion(direccion);
            concierto.setFecha(fecha);
            concierto.setContactoEmail(contactoEmail);
            concierto.setImagenUrl("/images/" + nombreArchivo);
            concierto.setArtista(artista);
            concierto.setArtistasApertura(artistasApertura);
            concierto.setRecinto(recinto);
            concierto.setCapacidad(capacidad != null ? capacidad : 0);
            concierto.setHora(horaComienzo);
            concierto.setAperturaPuertas(aperturaPuertas);
            concierto.setParking(parking != null ? parking : false);

            EventoConcierto nuevo = eventoService.saveConcierto(concierto);
            logger.info("Evento de concierto creado: {}", nuevo.getNombre());
            return ResponseEntity.ok(nuevo);

        } catch (Exception e) {
            logger.error("Error creando evento de concierto", e);
            return ResponseEntity.status(500).body("Error al crear el evento de concierto");
        }
    }
    
    @GetMapping
    public ResponseEntity<List<EventoConcierto>> getAllConciertos() {
        return ResponseEntity.ok(eventoService.getAllConciertos());
    }
}
