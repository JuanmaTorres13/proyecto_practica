package com.eventzone.eventzone.model;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_cine")
public class EventoCine extends Evento {
	
	private String idioma;            
    private String tituloPelicula;    
    private String director;
    private String clasificacion;
    private String sala;         
    private Integer asientos;     
    private LocalTime horarioSesion;     
    
    
    public EventoCine() {
    }
    
    
    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }
    
    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public void setCineTitulo(String tituloPelicula) {
        this.tituloPelicula = tituloPelicula;
    }
    
    public String getDirector() {
        return director;
    }

    public void setCineDirector(String director) {
        this.director = director;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }
    
    public String getSala() {
        return sala;
    }

    public void setCineSala(String sala) {
        this.sala = sala;
    }

    public Integer getAsientos() {
        return asientos;
    }

    public void setCineAsientos(Integer asientos) {
        this.asientos = asientos;
    }

    public LocalTime getHorarioSesion() {
        return horarioSesion;
    }

    public void setCineHorarios(LocalTime horarioSesion) {
        this.horarioSesion = horarioSesion;
    }
    
	
}