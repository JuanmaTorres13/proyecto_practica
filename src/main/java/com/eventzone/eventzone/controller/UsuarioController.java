package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.dto.LoginRequest;
import com.eventzone.eventzone.dto.RegistroRequest;
import com.eventzone.eventzone.dto.UsuarioResponse;
import com.eventzone.eventzone.exception.EmailAlreadyExistsException;
import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.security.JwtUtil;
import com.eventzone.eventzone.security.UsuarioDetailsService;
import com.eventzone.eventzone.service.UsuarioService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador que gestiona todas las operaciones relacionadas con usuarios.
 * <p>
 * Este controlador maneja:
 * <ul>
 *     <li>Registro de usuarios</li>
 *     <li>Login y logout</li>
 *     <li>Gestión del perfil del usuario autenticado</li>
 *     <li>Listado de todos los usuarios (solo admin)</li>
 * </ul>
 * </p>
 * 
 * <p>Los tokens JWT se generan en el login y se almacenan en cookies para
 * la autenticación en rutas protegidas.</p>
 * 
 * @author 
 * @version 1.1
 * @since 2025
 */
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

    // ==================== VISTAS ====================

    /**
     * Muestra el formulario de login de usuarios.
     * 
     * @return nombre de la plantilla Thymeleaf para login
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "usuarios/login";
    }

    /**
     * Muestra la vista del perfil del usuario autenticado.
     * 
     * @return nombre de la plantilla Thymeleaf para perfil
     */
    @GetMapping("/profile")
    public String mostrarPerfil() {
        return "usuarios/profile";
    }  

    // ==================== REGISTRO ====================

    /**
     * Registra un nuevo usuario en la base de datos.
     * 
     * @param request DTO con los datos de registro del usuario
     * @return ResponseEntity con mensaje de éxito o error
     */
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

    // ==================== LOGIN ====================

    /**
     * Autentica un usuario y genera un token JWT en cookie HTTP-only.
     * 
     * @param loginRequest objeto con email y contraseña
     * @param response     objeto HttpServletResponse para agregar cookie
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            if (!usuarioService.existePorEmail(loginRequest.getEmail())) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            // Obtener el rol real del usuario
            Usuario usuario = usuarioService.buscarPorEmail(loginRequest.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "Login exitoso",
                "rol", usuario.getRol().getNombre()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al iniciar sesión");
        }
    }

    // ==================== LOGOUT ====================

    /**
     * Cierra la sesión del usuario eliminando la cookie JWT.
     * 
     * @param response HttpServletResponse para eliminar cookie y redirigir
     * @throws IOException si ocurre un error al redirigir
     */
    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); 
        response.addCookie(cookie);
        response.sendRedirect("/usuarios/login");
    }

    // ==================== PERFIL ====================

    /**
     * Obtiene los datos del usuario autenticado usando token JWT.
     * 
     * @return ResponseEntity con UsuarioResponse o error 401 si no está autenticado
     */
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
            usuario.getRol().getNombre(),
            usuario.getTelefono(),
            usuario.getCiudad(),
            usuario.getBio(),
            usuario.getFechaNacimiento()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     * 
     * @param usuarioResponse DTO con los datos actualizados
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PutMapping("/me")
    @ResponseBody
    public ResponseEntity<?> actualizarPerfil(@RequestBody UsuarioResponse usuarioResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = (String) authentication.getPrincipal();
        Usuario usuario = usuarioService.buscarPorEmail(email);

        usuario.setNombre(usuarioResponse.getNombre());
        usuario.setEmail(usuarioResponse.getEmail());
        usuario.setTelefono(usuarioResponse.getTelefono());
        usuario.setCiudad(usuarioResponse.getCiudad());
        usuario.setBio(usuarioResponse.getBio());
        usuario.setFechaNacimiento(usuarioResponse.getFechaNacimiento());

        usuarioService.guardar(usuario);

        return ResponseEntity.ok("Perfil actualizado correctamente");
    }

    // ==================== LISTAR TODOS LOS USUARIOS (ADMIN) ====================

    /**
     * Devuelve la lista de todos los usuarios registrados.
     * <p>Este endpoint está pensado para administradores.</p>
     * 
     * @return ResponseEntity con lista de usuarios o error 500
     */
    @GetMapping("/todos")
    @ResponseBody
    public ResponseEntity<?> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener usuarios");
        }
    }
    
    @DeleteMapping("/eliminar/{email}")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable String email) {
    	
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (usuario == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        usuarioService.eliminar(usuario.getId());
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
