package com.eventzone.eventzone.service;

import com.eventzone.eventzone.model.Compra;
import com.eventzone.eventzone.model.Ticket;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.repository.CompraRepository;
import com.eventzone.eventzone.repository.TicketRepository;
import com.eventzone.eventzone.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Compra comprarTicket(Usuario usuario, Long eventoId) throws Exception {
        List<Ticket> tickets = ticketRepository.findByEventoId(eventoId);

        if (tickets.size()<1){
            throw new Exception("No hay tickets disponibles");
        }

        Ticket ticket = tickets.get(0);

        if (ticket.getDisponibles() <= 0) {
            throw new Exception("No hay tickets disponibles");
        }

        // Incrementar vendidos y actualizar estado del ticket
        ticket.setVendidos(ticket.getVendidos() + 1);
        ticket.actualizarEstadoVenta();
        ticketRepository.save(ticket);

        // Crear la compra
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setTicket(ticket);
        compra.setCantidad(1);
        compra.setTotal(ticket.getPrecio() * 1);
        compra.setFechaCompra(LocalDateTime.now());

        return compraRepository.save(compra);
    }

    public List<Compra> listarComprasPorUsuario(Usuario usuario) {
        return compraRepository.findAllByUsuarioWithTicketAndEvento(usuario);
    }
}