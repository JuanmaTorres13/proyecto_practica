package com.eventzone.eventzone.service;

import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    // ===== CRUD =====
    public Ticket guardar(Ticket ticket) {
        if (ticket.getVendidos() == null) ticket.setVendidos(0);
        return ticketRepository.save(ticket);
    }

    public Ticket buscarPorId(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        ticketRepository.deleteById(id);
    }

    public List<Ticket> listarPorEvento(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId);
    }

    // ===== ESTADÍSTICAS =====
    public long totalTicketsVendidos(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId)
                .stream()
                .mapToLong(Ticket::getVendidos)
                .sum();
    }

    public long totalTicketsDisponibles(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId)
                .stream()
                .mapToLong(Ticket::getDisponibles)
                .sum();
    }

    public double ingresosTotales(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId)
                .stream()
                .mapToDouble(t -> t.getVendidos() * t.getPrecio())
                .sum();
    }

    public Map<String, Long> ticketsVendidosPorTipo(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId)
                .stream()
                .collect(Collectors.groupingBy(
                        Ticket::getTipo,
                        Collectors.summingLong(Ticket::getVendidos)
                ));
    }
}
