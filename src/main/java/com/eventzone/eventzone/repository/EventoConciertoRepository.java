package com.eventzone.eventzone.repository;

import com.eventzone.eventzone.model.EventoConcierto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoConciertoRepository extends JpaRepository<EventoConcierto, Long> {
}