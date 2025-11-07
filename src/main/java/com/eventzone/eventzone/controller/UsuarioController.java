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

/**
 * Controlador principal que gestiona todas las operaciones relacionadas con los usuarios.
 * <p>
 * Se encarga de manejar el registro, login, logout, verificación de credenciales
 * y gestión del perfil del usuario autenticado.
 * </p>
 *
 * <p>Los tokens JWT se generan en el login y se guardan en cookies seguras
 * para la autenticación en las rutas protegidas.</p>
 *
 * @author  
 * @version 1.0
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
     * Muestra la vista del formulario de login.
     *
     * @return nombre de la plantilla Thymeleaf correspondiente.
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "usuarios/login";
    }

    /**
     * Muestra la vista del perfil del usuario.
     *
     * @return nombre de la plantilla Thymeleaf correspondiente.
     */
    @GetMapping("/profile")
    public String mostrarPerfil() {
        return "usuarios/profile";
    }

    // ==================== REGISTRO ====================

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param request DTO con los datos del registro del usuario.
     * @return respuesta HTTP indicando éxito o error.
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
     * Autentica un usuario, genera un token JWT y lo guarda en una cookie.
     * <p>
     * Si las credenciales son correctas, redirige al perfil del usuario.
     * </p>
     *
     * @param loginRequest objeto con email y contraseña.
     * @param response     respuesta HTTP donde se agregará la cookie JWT.
     * @return una redirección al perfil o un error en caso de fallo.
     */
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

        // 🔐 Crear cookie con el token JWT
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // cambia a true en producción (HTTPS)
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

    // ==================== LOGOUT ====================

    /**
     * Cierra la sesión del usuario eliminando la cookie JWT.
     *
     * @param response objeto de respuesta HTTP.
     * @throws IOException si ocurre un error al redirigir.
     */
    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // elimina la cookie
        response.addCookie(cookie);
        response.sendRedirect("/usuarios/login");
    }

    // ==================== VERIFICACIÓN ====================

    /**
     * Verifica si un usuario existe y si sus credenciales son correctas.
     * <p>
     * Si es correcto, redirige al perfil del usuario, de lo contrario,
     * redirige al login con un mensaje de error.
     * </p>
     *
     * @param loginRequest credenciales del usuario.
     * @param response     objeto HTTP para redirección.
     * @throws IOException si ocurre un error durante la redirección.
     */
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

        response.sendRedirect("/usuarios/profile");
    }

    // ==================== PERFIL ====================

    /**
     * Obtiene la información del usuario autenticado usando el token JWT.
     *
     * @return {@link UsuarioResponse} con los datos del usuario autenticado.
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
     * Actualiza los datos del perfil del usuario autenticado en la base de datos.
     *
     * @param usuarioResponse objeto con los nuevos datos del usuario.
     * @return mensaje de confirmación o error.
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
}
