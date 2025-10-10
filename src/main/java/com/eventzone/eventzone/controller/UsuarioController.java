package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.dto.RegistroRequest;
import com.eventzone.eventzone.exception.EmailAlreadyExistsException;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.security.JwtAuthenticationFilter;
import com.eventzone.eventzone.service.UsuarioService;
import com.eventzone.eventzone.dto.UsuarioResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
	
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroRequest request) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(request);
            return ResponseEntity.ok("Usuario registrado correctamente con email: " + nuevoUsuario.getEmail());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al registrar el usuario");
        }
    }

    
    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            String userEmail = (String) authentication.getPrincipal();
            Usuario usuario = usuarioService.buscarPorEmail(userEmail);

            UsuarioResponse response = new UsuarioResponse(
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log completo del error
            return ResponseEntity.internalServerError().body("Error al obtener usuario autenticado");
        }
    }

}
