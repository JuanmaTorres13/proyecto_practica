package com.eventzone.eventzone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/usuarios/login")
    public String mostrarLogin() {
        return "usuarios/login";
    }

    @GetMapping("/usuarios/perfil")
    public String mostrarPerfil() {
        return "usuarios/perfil";
    }
}
