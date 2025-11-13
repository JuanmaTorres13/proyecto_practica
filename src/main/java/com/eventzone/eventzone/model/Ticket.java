package com.eventzone.eventzone.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private double precio;
    private Integer cantidad;
    private Integer vendidos = 0;
    private String descripcion;

    private boolean vendido = false; 

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    // ===== Getters y Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Integer getVendidos() { return vendidos; }
    public void setVendidos(Integer vendidos) { this.vendidos = vendidos; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isVendido() { return vendido; }  
    public void setVendido(boolean vendido) { this.vendido = vendido; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    // ===== Método calculado =====
    @Transient
    public Integer getDisponibles() {
        return cantidad != null && vendidos != null ? cantidad - vendidos : 0;
    }

    // ===== Lógica opcional =====
    /**
     * Marca el ticket como vendido si ya no quedan disponibles.
     */
    public void actualizarEstadoVenta() {
        this.vendido = (vendidos != null && cantidad != null && vendidos >= cantidad);
    }
}
