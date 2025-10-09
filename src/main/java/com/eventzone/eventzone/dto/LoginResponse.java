package com.eventzone.eventzone.dto;

public class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() {
        return token;
    }

    // Setter (opcional)
    public void setToken(String token) {
        this.token = token;
    }
}
