package com.eventzone.eventzone.model;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_concierto")
public class EventoConcierto extends Evento {

	private String artista;
	private String artistasApertura;
	private String recinto;
	private Integer capacidad;
	private LocalTime horaComienzo;
	private LocalTime aperturaPuertas;
	private Boolean parking;

	public EventoConcierto() {
	}

	public String getArtista() {
		return artista;
	}

	public void setArtista(String artista) {
		this.artista = artista;
	}

	public String getArtistasApertura() {
		return artistasApertura;
	}

	public void setArtistasApertura(String artistasApertura) {
		this.artistasApertura = artistasApertura;
	}

	public String getRecinto() {
		return recinto;
	}

	public void setRecinto(String recinto) {
		this.recinto = recinto;
	}

	public Integer getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(Integer capacidad) {
		this.capacidad = capacidad;
	}

	public LocalTime getHoraComienzo() {
		return horaComienzo;
	}

	public void setHora(LocalTime horaComienzo) {
		this.horaComienzo = horaComienzo;
	}

	public LocalTime getAperturaPuertas() {
		return aperturaPuertas;
	}

	public void setAperturaPuertas(LocalTime aperturaPuertas) {
		this.aperturaPuertas = aperturaPuertas;
	}

	public Boolean getParking() {
		return parking;
	}

	public void setParking(Boolean parking) {
		this.parking = parking;
	}
}