package com.eventzone.eventzone.security;

import com.eventzone.eventzone.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Clase que implementa {@link UserDetails} y actúa como un adaptador entre la entidad
 * {@link Usuario} y el sistema de autenticación de Spring Security.
 * 
 * <p>Esta clase encapsula los datos necesarios para la autenticación y autorización
 * del usuario dentro del contexto de seguridad de Spring.</p>
 * 
 * <p>Incluye el email, la contraseña y los roles del usuario como autoridades de seguridad.</p>
 * 
 * @author 
 * @version 1.0
 */
public class UsuarioPrincipal implements UserDetails {

    /** Email del usuario, utilizado como nombre de usuario en Spring Security. */
    private final String email;

    /** Contraseña encriptada del usuario. */
    private final String password;

    /** Colección de autoridades o roles asociados al usuario. */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor principal que inicializa las credenciales y autoridades del usuario.
     *
     * @param email Email del usuario.
     * @param password Contraseña encriptada del usuario.
     * @param authorities Lista de roles o permisos del usuario.
     */
    public UsuarioPrincipal(String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Crea una instancia de {@link UsuarioPrincipal} a partir de un objeto {@link Usuario}.
     * 
     * <p>Convierte el rol del usuario en una autoridad de Spring Security.</p>
     *
     * @param usuario Entidad {@link Usuario} de la base de datos.
     * @return Objeto {@link UsuarioPrincipal} listo para ser usado por Spring Security.
     */
    public static UsuarioPrincipal build(Usuario usuario) {
        String rolNombre = usuario.getRol().getNombre().toUpperCase();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(rolNombre);

        return new UsuarioPrincipal(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(authority)
        );
    }

    /**
     * Devuelve la lista de autoridades o roles asociados al usuario.
     *
     * @return Colección de {@link GrantedAuthority}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Devuelve la contraseña encriptada del usuario.
     *
     * @return Contraseña del usuario.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Devuelve el email del usuario (usado como nombre de usuario).
     *
     * @return Email del usuario.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta del usuario no ha expirado.
     *
     * @return {@code true} si la cuenta sigue activa.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta del usuario no está bloqueada.
     *
     * @return {@code true} si la cuenta no está bloqueada.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales del usuario (contraseña) no han expirado.
     *
     * @return {@code true} si las credenciales son válidas.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado.
     *
     * @return {@code true} si el usuario está habilitado.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
