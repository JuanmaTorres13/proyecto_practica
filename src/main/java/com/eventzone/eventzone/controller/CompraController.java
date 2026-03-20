package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.dto.CompraDTO;
import com.eventzone.eventzone.model.Compra;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.service.CompraService;
import com.eventzone.eventzone.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/ticket/{eventoId}")
    public ResponseEntity<?> comprarTicket(@PathVariable Long eventoId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName(); // asumimos que el email está en principal
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            Compra compra = compraService.comprarTicket(usuario, eventoId);
            return ResponseEntity.ok(compra);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-compras")
    public ResponseEntity<?> misCompras() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            List<Compra> compras = compraService.listarComprasPorUsuario(usuario);

            // Convertir a DTO
            List<CompraDTO> comprasDTO = compras.stream()
                    .map(c -> {
                        var ticket = c.getTicket();
                        var evento = ticket.getEvento();
                        return new CompraDTO(
                                c.getId(),
                                ticket.getTipo(),
                                ticket.getPrecio(),
                                c.getCantidad(),
                                evento.getNombre(),
                                evento.getCiudad(),
                                evento.getDireccion(),
                                evento.getImagenUrl(),
                                c.getFechaCompra()
                        );
                    })
                    .toList();

            return ResponseEntity.ok(comprasDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}