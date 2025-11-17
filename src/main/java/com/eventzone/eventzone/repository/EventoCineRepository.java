package com.eventzone.eventzone.repository;

import com.eventzone.eventzone.model.EventoCine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoCineRepository extends JpaRepository<EventoCine, Long> {
}
