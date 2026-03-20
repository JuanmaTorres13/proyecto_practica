package com.eventzone.eventzone.dto;

import java.time.LocalDateTime;

public class CompraDTO {

    private Long compraId;
    private String ticketTipo;
    private double ticketPrecio;
    private Integer cantidad;
    private String eventoNombre;
    private String eventoCiudad;
    private String eventoDireccion;
    private String eventoImagenUrl;
    private LocalDateTime fechaCompra;

    // ===== Constructor =====
    public CompraDTO(Long compraId, String ticketTipo, double ticketPrecio, Integer cantidad,
                     String eventoNombre, String eventoCiudad, String eventoDireccion, String eventoImagenUrl,
                     LocalDateTime fechaCompra) {
        this.compraId = compraId;
        this.ticketTipo = ticketTipo;
        this.ticketPrecio = ticketPrecio;
        this.cantidad = cantidad;
        this.eventoNombre = eventoNombre;
        this.eventoCiudad = eventoCiudad;
        this.eventoDireccion = eventoDireccion;
        this.eventoImagenUrl = eventoImagenUrl;
        this.fechaCompra = fechaCompra;
    }

    // ===== Getters =====
    public Long getCompraId() { return compraId; }
    public String getTicketTipo() { return ticketTipo; }
    public double getTicketPrecio() { return ticketPrecio; }
    public Integer getCantidad() { return cantidad; }
    public String getEventoNombre() { return eventoNombre; }
    public String getEventoCiudad() { return eventoCiudad; }
    public String getEventoDireccion() { return eventoDireccion; }
    public String getEventoImagenUrl() { return eventoImagenUrl; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
}