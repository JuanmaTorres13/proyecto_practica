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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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

    @GetMapping("/profile")
    public String mostrarPerfil() {
        return "usuarios/profile";
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {
        logger.info("Intentando login para usuario: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(loginRequest.getEmail());
        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

        // Crear cookie JWT
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // cambia a true si usas HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        logger.info("Login exitoso, cookie JWT creada para usuario: {}", loginRequest.getEmail());

        try {
            response.sendRedirect("/usuarios/profile");
            return null;
        } catch (IOException e) {
            logger.error("Error al redirigir después del login", e);
            return ResponseEntity.internalServerError().body("Error en la redirección");
        }
    }

    
    
    // --- LOGOUT ---
    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // elimina la cookie
        response.addCookie(cookie);
        response.sendRedirect("/usuarios/login");
    }


    
    @PostMapping("/verificar")
    public void verificarUsuario(LoginRequest loginRequest, HttpServletResponse response) throws IOException {
        if (!usuarioService.existePorEmail(loginRequest.getEmail())) {
            response.sendRedirect("/usuarios/login?error=usuario_no_existe");
            return;
        }

        boolean correcto = usuarioService.verificarCredenciales(
            loginRequest.getEmail(), loginRequest.getPassword()
        );

        if (!correcto) {
            response.sendRedirect("/usuarios/login?error=contraseña_incorrecta");
            return;
        }

        // ✅ Si todo va bien, redirigir al perfil
        response.sendRedirect("/usuarios/profile");
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
