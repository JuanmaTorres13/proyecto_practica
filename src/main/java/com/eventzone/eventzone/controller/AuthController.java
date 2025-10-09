package com.eventzone.eventzone.controller;

import com.eventzone.eventzone.dto.LoginRequest;
import com.eventzone.eventzone.dto.LoginResponse;
import com.eventzone.eventzone.dto.RegistroRequest;
import com.eventzone.eventzone.exception.EmailAlreadyExistsException;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.security.JwtUtil;
import com.eventzone.eventzone.security.UsuarioDetailsService;
import com.eventzone.eventzone.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(loginRequest.getEmail());

        String token = jwtUtil.generateToken(userDetails.getUsername());

        return new LoginResponse(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest registroRequest) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(registroRequest);
            return ResponseEntity.ok("Usuario registrado con éxito: " + nuevoUsuario.getEmail());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
