package com.eventzone.eventzone.config;

import com.eventzone.eventzone.model.Rol;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.repository.RolRepository;
import com.eventzone.eventzone.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepo,
            RolRepository rolRepo,
            PasswordEncoder passwordEncoder) {
        return args -> {

            // Crear rol ADMIN si no existe
            Rol rolAdmin = rolRepo.findByNombre("ADMIN")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("ADMIN");
                        return rolRepo.save(nuevoRol);
                    });

            // Crear rol USER si no existe
            Rol rolUser = rolRepo.findByNombre("USER")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("USER");
                        return rolRepo.save(nuevoRol);
                    });

            // Verificar si usuario admin ya existe
            Optional<Usuario> adminExistente = usuarioRepo.findByEmail("admin@eventzone.com");
            if (adminExistente.isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail("admin@eventzone.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFechaRegistro(LocalDateTime.now());
                admin.setRol(rolAdmin); // ⚠ Ahora es campo único

                usuarioRepo.save(admin);
                System.out.println("Usuario administrador creado con email admin@eventzone.com y password admin123");
            } else {
                System.out.println("Usuario administrador ya existe");
            }

            System.out.println("Roles inicializados correctamente.");
        };
    }
}
