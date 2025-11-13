package com.eventzone.eventzone.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;             // cine, concierto, festival
    private String nombre;
    @Column(length = 5000)
    private String descripcion;

    private String genero;

    private String imagenUrl;
    private String duracion;
    private String idioma;            // solo cine

    private String artista;           // solo concierto
    private String artistasApertura;  // solo concierto

    private String festivalLineup;    // solo festival
    private Integer festivalDias;     // solo festival

    private String cineTitulo;        // solo cine
    private String cineDirector;      // solo cine
    private String clasificacion;     // cine

    private String cineNombre;        // cine
    private String cineSala;          // cine
    private Integer cineAsientos;     // cine
    private String cineHorarios;      // cine

    private String recinto;           // concierto/festival
    private String ciudad;
    private String direccion;
    private Integer capacidad;        // concierto/festival

    private LocalDate fecha;
    private LocalTime hora;           // concierto/festival
    private LocalTime aperturaPuertas; // concierto/festival
    private LocalDate fechaFin;       // festival

    private String restriccionesEdad;
    @Column(length = 2000)
    private String normas;
    private String contactoEmail;

    private Boolean parking;
    private Boolean accesible;
    private Boolean comida;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    // ===== Getters y Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
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

    public String getFestivalLineup() {
        return festivalLineup;
    }

    public void setFestivalLineup(String festivalLineup) {
        this.festivalLineup = festivalLineup;
    }

    public Integer getFestivalDias() {
        return festivalDias;
    }

    public void setFestivalDias(Integer festivalDias) {
        this.festivalDias = festivalDias;
    }

    public String getCineTitulo() {
        return cineTitulo;
    }

    public void setCineTitulo(String cineTitulo) {
        this.cineTitulo = cineTitulo;
    }

    public String getCineDirector() {
        return cineDirector;
    }

    public void setCineDirector(String cineDirector) {
        this.cineDirector = cineDirector;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getCineNombre() {
        return cineNombre;
    }

    public void setCineNombre(String cineNombre) {
        this.cineNombre = cineNombre;
    }

    public String getCineSala() {
        return cineSala;
    }

    public void setCineSala(String cineSala) {
        this.cineSala = cineSala;
    }

    public Integer getCineAsientos() {
        return cineAsientos;
    }

    public void setCineAsientos(Integer cineAsientos) {
        this.cineAsientos = cineAsientos;
    }

    public String getCineHorarios() {
        return cineHorarios;
    }

    public void setCineHorarios(String cineHorarios) {
        this.cineHorarios = cineHorarios;
    }

    public String getRecinto() {
        return recinto;
    }

    public void setRecinto(String recinto) {
        this.recinto = recinto;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public LocalTime getAperturaPuertas() {
        return aperturaPuertas;
    }

    public void setAperturaPuertas(LocalTime aperturaPuertas) {
        this.aperturaPuertas = aperturaPuertas;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getRestriccionesEdad() {
        return restriccionesEdad;
    }

    public void setRestriccionesEdad(String restriccionesEdad) {
        this.restriccionesEdad = restriccionesEdad;
    }

    public String getNormas() {
        return normas;
    }

    public void setNormas(String normas) {
        this.normas = normas;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public void setContactoEmail(String contactoEmail) {
        this.contactoEmail = contactoEmail;
    }

    public Boolean getParking() {
        return parking;
    }

    public void setParking(Boolean parking) {
        this.parking = parking;
    }

    public Boolean getAccesible() {
        return accesible;
    }

    public void setAccesible(Boolean accesible) {
        this.accesible = accesible;
    }

    public Boolean getComida() {
        return comida;
    }

    public void setComida(Boolean comida) {
        this.comida = comida;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setEvento(this);
    }

    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
        ticket.setEvento(null);
    }
}
