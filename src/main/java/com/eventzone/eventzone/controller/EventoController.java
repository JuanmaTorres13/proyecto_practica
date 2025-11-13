package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // ===== VISTAS =====
    @GetMapping("/crear")
    public String mostrarCrearEvento() { return "eventos/crear"; }

    @GetMapping("/lista")
    public String listarEventos(org.springframework.ui.Model model) {
        model.addAttribute("eventos", eventoService.listarTodos());
        return "eventos/lista";
    }


    @GetMapping("/{id}")
    public String mostrarEvento(@PathVariable Long id) { return "eventos/ver"; }

    // ===== CRUD EVENTOS =====
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<?> crearEvento(@RequestBody Evento evento) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden crear eventos");
        Evento nuevoEvento = eventoService.guardar(evento);
        return ResponseEntity.ok(nuevoEvento);
    }

    @GetMapping("/todos")
    @ResponseBody
    public ResponseEntity<?> listarEventos() {
        List<Evento> eventos = eventoService.listarTodos();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/detalle/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerEvento(@PathVariable Long id) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) return ResponseEntity.status(404).body("Evento no encontrado");
        return ResponseEntity.ok(evento);
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarEvento(@PathVariable Long id, @RequestBody Evento evento) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden editar eventos");

        Evento existente = eventoService.buscarPorId(id);
        if (existente == null) return ResponseEntity.status(404).body("Evento no encontrado");

        // Actualizar campos básicos
        existente.setTipo(evento.getTipo());
        existente.setNombre(evento.getNombre());
        existente.setDescripcion(evento.getDescripcion());
        existente.setGenero(evento.getGenero());
        existente.setImagenUrl(evento.getImagenUrl());
        existente.setDuracion(evento.getDuracion());
        existente.setIdioma(evento.getIdioma());
        existente.setArtista(evento.getArtista());
        existente.setArtistasApertura(evento.getArtistasApertura());
        existente.setFestivalLineup(evento.getFestivalLineup());
        existente.setFestivalDias(evento.getFestivalDias());
        existente.setCineTitulo(evento.getCineTitulo());
        existente.setCineDirector(evento.getCineDirector());
        existente.setClasificacion(evento.getClasificacion());
        existente.setCineNombre(evento.getCineNombre());
        existente.setCineSala(evento.getCineSala());
        existente.setCineAsientos(evento.getCineAsientos());
        existente.setCineHorarios(evento.getCineHorarios());
        existente.setRecinto(evento.getRecinto());
        existente.setCiudad(evento.getCiudad());
        existente.setDireccion(evento.getDireccion());
        existente.setCapacidad(evento.getCapacidad());
        existente.setFecha(evento.getFecha());
        existente.setFechaFin(evento.getFechaFin());
        existente.setHora(evento.getHora());
        existente.setAperturaPuertas(evento.getAperturaPuertas());
        existente.setRestriccionesEdad(evento.getRestriccionesEdad());
        existente.setNormas(evento.getNormas());
        existente.setContactoEmail(evento.getContactoEmail());
        existente.setParking(evento.getParking());
        existente.setAccesible(evento.getAccesible());
        existente.setComida(evento.getComida());

        // Actualizar tickets asociados
        if (evento.getTickets() != null) {
            existente.getTickets().clear();
            for (Ticket t : evento.getTickets()) {
                t.setEvento(existente);
                existente.getTickets().add(t);
            }
        }

        eventoService.guardar(existente);
        return ResponseEntity.ok(existente);
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEvento(@PathVariable Long id) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden eliminar eventos");

        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) return ResponseEntity.status(404).body("Evento no encontrado");

        eventoService.eliminar(id);
        return ResponseEntity.ok("Evento eliminado correctamente");
    }

    // ===== ESTADÍSTICAS DE TICKETS =====
    @GetMapping("/estadisticas/{id}")
    @ResponseBody
    public ResponseEntity<?> estadisticasEvento(@PathVariable Long id) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) return ResponseEntity.status(404).body("Evento no encontrado");

        long vendidos = eventoService.totalTicketsVendidos(evento);
        long disponibles = eventoService.totalTicketsDisponibles(evento);
        double ingresos = eventoService.ingresosTotales(evento);

        return ResponseEntity.ok(Map.of(
                "vendidos", vendidos,
                "disponibles", disponibles,
                "ingresos", ingresos
        ));
    }

    // ===== MÉTODO AUXILIAR =====
    private boolean usuarioEsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
