package com.eventzone.eventzone.repository;

import com.eventzone.eventzone.model.Compra;
import com.eventzone.eventzone.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("SELECT c FROM Compra c " +
            "JOIN FETCH c.ticket t " +
            "JOIN FETCH t.evento " +
            "WHERE c.usuario = :usuario")
    List<Compra> findAllByUsuarioWithTicketAndEvento(@Param("usuario") Usuario usuario);

}