package com.eventzone.eventzone.service;

import com.eventzone.eventzone.model.Evento;
import com.eventzone.eventzone.model.EventoCine;
import com.eventzone.eventzone.model.EventoConcierto;
import com.eventzone.eventzone.model.EventoFestival;
import com.eventzone.eventzone.repository.EventoCineRepository;
import com.eventzone.eventzone.repository.EventoConciertoRepository;
import com.eventzone.eventzone.repository.EventoFestivalRepository;
import com.eventzone.eventzone.repository.EventoRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class EventoService {

	private final EventoRepository eventoRepository;
	private final EventoCineRepository cineRepository;
	private final EventoConciertoRepository conciertoRepository;
	private final EventoFestivalRepository festivalRepository;
	
	public EventoService(EventoRepository eventoRepository,
						EventoCineRepository cineRepository,
						EventoConciertoRepository conciertoRepository,
						EventoFestivalRepository festivalRepository) {
		this.eventoRepository = eventoRepository;
		this.cineRepository = cineRepository;
		this.conciertoRepository = conciertoRepository;
		this.festivalRepository = festivalRepository;
	}
	
	public List<Evento> getAllEvents(){
		return eventoRepository.findAll();
	}
	
	public Optional<Evento> getEventbyId(Long id){
		return eventoRepository.findById(id);
	}

	public Evento saveEvent(Evento evento) {
		return eventoRepository.save(evento);
	}

	public void deleteEvent(Long id) {
		eventoRepository.deleteById(id);
	}

	
	public List<EventoCine> getAllCines(){
		return cineRepository.findAll();
	}
	
	public Optional<EventoCine> getCinebyId(Long id){
		return cineRepository.findById(id);
	}
	
	public EventoCine saveCine(EventoCine cine) {
		return cineRepository.save(cine);
	}
	
	public void deleteCine(Long id) {
		cineRepository.deleteById(id);
	}
	
	
	public List<EventoConcierto> getAllConciertos(){
		return conciertoRepository.findAll();
	}
	
	public Optional<EventoConcierto> getConciertobyId(Long id){
		return conciertoRepository.findById(id);
	}
	
	public EventoConcierto saveConcierto(EventoConcierto concierto) {
		return conciertoRepository.save(concierto);
	}
	
	public void deleteConcierto(Long id) {
		conciertoRepository.deleteById(id);
	}
	
	
	public List <EventoFestival> getAllFestivales(){
		return festivalRepository.findAll();
	}
	
	public Optional <EventoFestival> getFestivalbyId(Long id){
		return festivalRepository.findById(id);
	}
	
	public EventoFestival saveFestival(EventoFestival festival) {
		return festivalRepository.save(festival);
	}
	
	public void deleteFestival (Long id) {
		festivalRepository.deleteById(id);
	}
	
	
	
}
