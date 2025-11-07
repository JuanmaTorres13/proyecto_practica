package com.eventzone.eventzone.service;

import com.eventzone.eventzone.dto.RegistroRequest;
import com.eventzone.eventzone.model.Rol;
import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.repository.RolRepository;
import com.eventzone.eventzone.repository.UsuarioRepository;
import com.eventzone.eventzone.exception.EmailAlreadyExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio que gestiona todas las operaciones relacionadas con los usuarios,
 * incluyendo registro, búsqueda, verificación de credenciales y actualización.
 * 
 * <p>Esta clase actúa como intermediario entre los controladores y el repositorio
 * de usuarios, aplicando la lógica de negocio correspondiente.</p>
 * 
 * @author 
 * @version 1.0
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Verifica que el correo electrónico no esté en uso, asigna el rol
     * por defecto "USER", codifica la contraseña y guarda el usuario en la base de datos.</p>
     *
     * @param request objeto {@link RegistroRequest} con los datos del nuevo usuario
     * @return el {@link Usuario} recién creado
     * @throws EmailAlreadyExistsException si el correo electrónico ya está registrado
     * @throws RuntimeException si no se encuentra el rol "USER"
     */
    public Usuario registrarUsuario(RegistroRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El email ya está en uso");
        }

        Rol rolUsuario = rolRepository.findByNombre("USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        nuevoUsuario.setRol(rolUsuario);

        return usuarioRepository.save(nuevoUsuario);
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email correo electrónico del usuario a buscar
     * @return el {@link Usuario} correspondiente al correo electrónico
     * @throws RuntimeException si no se encuentra el usuario
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Verifica si existe un usuario registrado con un correo electrónico dado.
     *
     * @param email correo electrónico a verificar
     * @return {@code true} si el usuario existe, {@code false} en caso contrario
     */
    public boolean existePorEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    /**
     * Comprueba si las credenciales de un usuario son correctas.
     *
     * <p>Busca el usuario por correo electrónico y compara la contraseña
     * ingresada con la almacenada (codificada).</p>
     *
     * @param email correo electrónico del usuario
     * @param password contraseña sin codificar ingresada por el usuario
     * @return {@code true} si las credenciales son válidas, {@code false} en caso contrario
     */
    public boolean verificarCredenciales(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        return passwordEncoder.matches(password, usuario.getPassword());
    }

    /**
     * Guarda o actualiza un usuario existente en la base de datos.
     *
     * <p>Este método puede utilizarse para actualizar los datos del perfil
     * del usuario, como nombre, email, ciudad o biografía.</p>
     *
     * @param usuario entidad {@link Usuario} a guardar
     * @return el {@link Usuario} persistido en la base de datos
     */
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
