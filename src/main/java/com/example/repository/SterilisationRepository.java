package com.example.repository;

import com.example.entity.Sterilisation;
import com.example.entity.enums.StatutSterilisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SterilisationRepository extends JpaRepository<Sterilisation, Long>, JpaSpecificationExecutor<Sterilisation> {
    List<Sterilisation> findByStatut(StatutSterilisation statut);
}