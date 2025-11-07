package com.eventzone.eventzone.dto;

import java.time.LocalDate;
import java.util.Date;

/**
 * DTO (Data Transfer Object) que representa la información pública del usuario
 * que se devuelve al cliente en las respuestas del backend.
 * <p>
 * Esta clase se utiliza principalmente para transferir datos de usuario sin
 * exponer información sensible como contraseñas o tokens.
 * </p>
 *
 * <p>Incluye datos básicos del perfil del usuario como nombre, email, rol, 
 * teléfono, ciudad, biografía y fecha de nacimiento.</p>
 *
 * @author  
 * @version 1.0
 * @since 2025
 */
public class UsuarioResponse {

    /** Nombre completo del usuario */
    private String nombre;

    /** Correo electrónico del usuario (usado como identificador principal) */
    private String email;

    /** Rol asignado al usuario (por ejemplo, USER o ADMIN) */
    private String rol;

    /** Número de teléfono del usuario */
    private String telefono;

    /** Ciudad de residencia del usuario */
    private String ciudad;

    /** Biografía o descripción personal del usuario */
    private String bio;

    /** Fecha de nacimiento del usuario */
    private Date fechaNacimiento;

    /**
     * Constructor completo para inicializar un objeto {@link UsuarioResponse}.
     *
     * @param nombre          nombre completo del usuario.
     * @param email           correo electrónico del usuario.
     * @param rol             rol asignado (USER, ADMIN, etc.).
     * @param telefono        número de teléfono del usuario.
     * @param ciudad          ciudad de residencia del usuario.
     * @param bio             breve descripción o biografía del usuario.
     * @param fechaNacimiento fecha de nacimiento del usuario.
     */
    public UsuarioResponse(String nombre, String email, String rol, String telefono, String ciudad, String bio, Date fechaNacimiento) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.bio = bio;
        this.fechaNacimiento = fechaNacimiento;
    }

    /** @return el nombre completo del usuario. */
    public String getNombre() {
        return nombre;
    }

    /** @return el correo electrónico del usuario. */
    public String getEmail() {
        return email;
    }

    /** @return el rol del usuario (USER, ADMIN, etc.). */
    public String getRol() {
        return rol;
    }

    /** @return el número de teléfono del usuario. */
    public String getTelefono() {
        return telefono;
    }

    /** @return la ciudad de residencia del usuario. */
    public String getCiudad() {
        return ciudad;
    }

    /** @return la biografía o descripción personal del usuario. */
    public String getBio() {
        return bio;
    }

    /** @return la fecha de nacimiento del usuario. */
    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }
}
