package com.eventzone.eventzone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.eventzone.eventzone.service.EventoService;

@Controller
@RequestMapping("/eventos")
public class EventoViewController {

    @Autowired
    private EventoService eventoService;

    @GetMapping("/detalle/{id}")
    public String verDetalleEvento(@PathVariable Long id, Model model) {

        var evento = eventoService.getEventbyId(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        model.addAttribute("evento", evento);

        return "usuarios/detalle_evento";
    }
}