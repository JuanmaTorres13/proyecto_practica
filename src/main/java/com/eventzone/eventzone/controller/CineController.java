package com.eventzone.eventzone.controller;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;


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
    public ResponseEntity<?> createCine(
    		@RequestParam("tipo") String tipo,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("direccion") String direccion,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("contactoEmail") String contactoEmail,
            @RequestParam("imagenFile") MultipartFile imagen,
            @RequestParam(required = false) String tituloPelicula,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String clasificacion,
            @RequestParam(required = false) String idioma
    ) {
        try {
        	
            String imagesDir = imagenService.guardarImagen(imagen);   

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
    
    
}
