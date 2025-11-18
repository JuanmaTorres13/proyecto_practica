package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // ===== CRUD =====
    @PostMapping("/crear")
    public ResponseEntity<?> crearTicket(@RequestBody Ticket ticket) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden crear tickets");
        return ResponseEntity.ok(ticketService.guardar(ticket));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.buscarPorId(id);
        if (ticket == null) return ResponseEntity.status(404).body("Ticket no encontrado");
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<?> listarTicketsPorEvento(@PathVariable Long eventoId) {
        List<Ticket> tickets = ticketService.listarPorEvento(eventoId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> actualizarTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden editar tickets");

        Ticket existente = ticketService.buscarPorId(id);
        if (existente == null) return ResponseEntity.status(404).body("Ticket no encontrado");

        existente.setTipo(ticket.getTipo());
        existente.setPrecio(ticket.getPrecio());
        existente.setCantidad(ticket.getCantidad());
        existente.setVendidos(ticket.getVendidos()); // actualizado correctamente

        ticketService.guardar(existente);
        return ResponseEntity.ok(existente);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarTicket(@PathVariable Long id) {
        if (!usuarioEsAdmin()) return ResponseEntity.status(403).body("Solo administradores pueden eliminar tickets");
        ticketService.eliminar(id);
        return ResponseEntity.ok("Ticket eliminado correctamente");
    }

    // ===== ESTADÍSTICAS =====
    @GetMapping("/estadisticas/{eventoId}")
    public ResponseEntity<?> estadisticasTickets(@PathVariable Long eventoId) {
        long vendidos = ticketService.totalTicketsVendidos(eventoId);
        long disponibles = ticketService.totalTicketsDisponibles(eventoId);
        double ingresos = ticketService.ingresosTotales(eventoId);
        Map<String, Long> porTipo = ticketService.ticketsVendidosPorTipo(eventoId);

        return ResponseEntity.ok(Map.of(
                "vendidos", vendidos,
                "disponibles", disponibles,
                "ingresos", ingresos,
                "porTipo", porTipo
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
