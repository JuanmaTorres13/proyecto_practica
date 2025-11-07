package com.eventzone.eventzone.dto;

/**
 * DTO que representa la solicitud de registro de un nuevo usuario.
 * Contiene los campos mínimos requeridos por el backend.
 */
public class RegistroRequest {
    private String nombre;
    private String email;
    private String password;

    /** @return el nombre completo del usuario */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre completo del usuario */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return el correo electrónico del usuario */
    public String getEmail() {
        return email;
    }

    /** @param email correo electrónico del usuario */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return la contraseña del usuario */
    public String getPassword() {
        return password;
    }

    /** @param password contraseña del usuario */
    public void setPassword(String password) {
        this.password = password;
    }
}
