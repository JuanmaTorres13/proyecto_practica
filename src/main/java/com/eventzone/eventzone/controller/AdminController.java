package com.eventzone.eventzone.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador que gestiona las vistas y funcionalidades del panel de administración.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * Muestra la vista principal del panel de administración.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @return nombre de la plantilla Thymeleaf para el panel de admin
     */
    @GetMapping("/panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String panelAdmin() {
        return "admin/admin_panel";
    }
}
