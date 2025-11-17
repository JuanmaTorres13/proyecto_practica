package com.eventzone.eventzone.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_festival")
public class EventoFestival extends Evento {

	private String cartelArtistas;
	private Integer diasDuracion;
	private LocalDate fechaFin;
	private String recinto;
	private Integer capacidad;
	private LocalTime horaComienzo;
	private LocalTime aperturaPuertas;
	private Boolean parking;

	public EventoFestival() {
	}

	public String getCartelArtistas() {
		return cartelArtistas;
	}

	public void setCartelArtistas(String cartelArtistas) {
		this.cartelArtistas = cartelArtistas;
	}

	public Integer getDiasDuracion() {
		return diasDuracion;
	}

	public void setFestivalDias(Integer diasDuracion) {
		this.diasDuracion = diasDuracion;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
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

	public Boolean getParkin() {
		return parking;
	}

	public void setParking(Boolean parking) {
		this.parking = parking;
	}
}