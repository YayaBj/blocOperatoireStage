package com.example.repository;

import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SalleRepository extends JpaRepository<Salle, Long>, JpaSpecificationExecutor<Salle> {

    Page<Salle> findAllBy(Pageable pageable);

    Salle findByNumeroSalle(String numeroSalle);

    List<Salle> findByStatutSalle(StatutSalle statutSalle);
}