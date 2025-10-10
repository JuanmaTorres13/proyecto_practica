package com.eventzone.eventzone.dto;

public class UsuarioResponse {
    private String nombre;
    private String email;
    private String rol;

    public UsuarioResponse(String nombre, String email, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
