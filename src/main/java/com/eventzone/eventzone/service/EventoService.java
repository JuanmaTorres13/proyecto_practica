package com.eventzone.eventzone.service;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.repository.EventoRepository;
import com.eventzone.eventzone.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // ==================== CRUD ====================

    public Evento guardar(Evento evento) {
        Evento savedEvento = eventoRepository.save(evento);

        if (evento.getTickets() != null) {
            for (Ticket ticket : evento.getTickets()) {
                ticket.setEvento(savedEvento);
                ticketRepository.save(ticket);
            }
        }

        return savedEvento;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento buscarPorId(Long id) {
        Optional<Evento> optional = eventoRepository.findById(id);
        return optional.orElse(null);
    }

    public void eliminar(Long id) {
        Evento evento = buscarPorId(id);
        if (evento != null) {
            if (evento.getTickets() != null) {
                for (Ticket t : evento.getTickets()) {
                    ticketRepository.delete(t);
                }
            }
            eventoRepository.delete(evento);
        }
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Total de tickets vendidos de un evento.
     */
    public long totalTicketsVendidos(Evento evento) {
        return evento.getTickets().stream()
                .filter(Ticket::isVendido)
                .count();
    }

    /**
     * Total de tickets disponibles (no vendidos).
     */
    public long totalTicketsDisponibles(Evento evento) {
        return evento.getTickets().stream()
                .filter(t -> !t.isVendido())
                .count();
    }

    /**
     * Ingresos totales del evento (suma de precios de tickets vendidos).
     */
    public double ingresosTotales(Evento evento) {
        return evento.getTickets().stream()
                .filter(Ticket::isVendido)
                .mapToDouble(Ticket::getPrecio)
                .sum();
    }
}
