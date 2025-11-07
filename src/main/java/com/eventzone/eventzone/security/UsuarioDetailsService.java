package com.eventzone.eventzone.security;

import com.eventzone.eventzone.model.Usuario;
import com.eventzone.eventzone.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Servicio que implementa la interfaz {@link UserDetailsService} de Spring Security.
 * <p>
 * Esta clase se encarga de recuperar la información del usuario desde la base de datos
 * durante el proceso de autenticación. A partir del correo electrónico proporcionado,
 * busca el {@link Usuario} correspondiente y construye un objeto {@link UsuarioPrincipal}
 * que Spring Security utiliza para gestionar la sesión autenticada.
 * </p>
 *
 * <p><b>Uso:</b></p>
 * <pre>
 *     UsuarioDetailsService userDetailsService = new UsuarioDetailsService();
 *     UserDetails user = userDetailsService.loadUserByUsername("correo@ejemplo.com");
 * </pre>
 *
 * @author
 * @version 1.0
 * @since 2025-11-07
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    /**
     * Repositorio para acceder a los datos de los usuarios almacenados en la base de datos.
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga los detalles de un usuario en función de su correo electrónico.
     * <p>
     * Este método se invoca automáticamente por Spring Security durante el proceso de autenticación.
     * Si el usuario existe en la base de datos, se construye y devuelve un {@link UsuarioPrincipal}.
     * Si no existe, se lanza una excepción {@link UsernameNotFoundException}.
     * </p>
     *
     * @param email el correo electrónico del usuario a buscar.
     * @return un objeto {@link UserDetails} que representa al usuario autenticado.
     * @throws UsernameNotFoundException si no se encuentra ningún usuario con el correo proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return UsuarioPrincipal.build(usuario);
    }
}
