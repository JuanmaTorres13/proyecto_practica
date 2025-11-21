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

import com.eventzone.eventzone.model.EventoCine;
import com.eventzone.eventzone.model.EventoFestival;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.EventoService;

@RestController
@RequestMapping("/eventos/festival")
public class FestivalController {

    private final EventoService eventoService;
    private static final Logger logger = LoggerFactory.getLogger(FestivalController.class);

    public FestivalController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> createFestival(
    		@RequestParam("tipo") String tipo,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("direccion") String direccion,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("contactoEmail") String contactoEmail,
            @RequestParam("imagenFile") MultipartFile imagen,
            @RequestParam(required = false) String cartelArtistas,
            @RequestParam(required = false) Integer diasDuracion,
            @RequestParam(required = false) String fechaFinStr,
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
            LocalDate fechaFin = (fechaFinStr != null && !fechaFinStr.isEmpty()) ? LocalDate.parse(fechaFinStr) : null;
            LocalTime horaComienzo = (horaComienzoStr != null) ? LocalTime.parse(horaComienzoStr) : null;
            LocalTime aperturaPuertas = (aperturaPuertasStr != null) ? LocalTime.parse(aperturaPuertasStr) : null;

            EventoFestival festival = new EventoFestival();
            festival.setTipo(tipo);
            festival.setNombre(nombre);
            festival.setDescripcion(descripcion);
            festival.setCiudad(ciudad);
            festival.setDireccion(direccion);
            festival.setFecha(fecha);
            festival.setFechaFin(fechaFin);
            festival.setContactoEmail(contactoEmail);
            festival.setImagenUrl("/images/" + nombreArchivo);
            festival.setCartelArtistas(cartelArtistas);
            festival.setFestivalDias(diasDuracion != null ? diasDuracion : 0);
            festival.setRecinto(recinto);
            festival.setCapacidad(capacidad != null ? capacidad : 0);
            festival.setHora(horaComienzo);
            festival.setAperturaPuertas(aperturaPuertas);
            festival.setParking(parking != null ? parking : false);

            EventoFestival nuevo = eventoService.saveFestival(festival);
            logger.info("Evento de festival creado: {}", nuevo.getNombre());
            return ResponseEntity.ok(nuevo);

        } catch (Exception e) {
            logger.error("Error creando evento de festival", e);
            return ResponseEntity.status(500).body("Error al crear el evento de festival");
        }
    }
    
    @GetMapping
    public ResponseEntity<List<EventoFestival>> getAllFestivales() {
        return ResponseEntity.ok(eventoService.getAllFestivales());
    }
}
