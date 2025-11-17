package com.eventzone.eventzone.repository;

import com.eventzone.eventzone.model.EventoFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoFestivalRepository extends JpaRepository<EventoFestival, Long> {
}