package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.dto.LoginRequest;
import com.eventzone.eventzone.dto.LoginResponse;
import com.eventzone.eventzone.dto.RegistroRequest;
import com.eventzone.eventzone.dto.UsuarioResponse;
import com.eventzone.eventzone.exception.EmailAlreadyExistsException;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.security.JwtUtil;
import com.eventzone.eventzone.security.UsuarioDetailsService;
import com.eventzone.eventzone.service.UsuarioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // --- VISTAS ---
    @GetMapping("/login")
    public String mostrarLogin() {
        return "usuarios/login";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil() {
        return "usuarios/perfil";
    }

    // --- REGISTRO ---
    @PostMapping("/registro")
    @ResponseBody
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

    // --- LOGIN ---
    @PostMapping("/login")
    @ResponseBody
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        logger.info("Intentando login para usuario: {}", loginRequest.getEmail());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(loginRequest.getEmail());

        String token = jwtUtil.generateToken(
                userDetails.getUsername(),
                userDetails.getAuthorities()
        );

        logger.info("Login exitoso para usuario: {}. Token generado.", loginRequest.getEmail());
        return new LoginResponse(token);
    }

    // --- PERFIL ---
    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String userEmail = (String) authentication.getPrincipal();
        Usuario usuario = usuarioService.buscarPorEmail(userEmail);

        UsuarioResponse response = new UsuarioResponse(
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol().getNombre()
        );

        return ResponseEntity.ok(response);
    }
}
