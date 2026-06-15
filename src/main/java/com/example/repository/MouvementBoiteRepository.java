package com.example.repository;

import com.example.entity.MouvementBoite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementBoiteRepository extends JpaRepository<MouvementBoite, Long> {

    List<MouvementBoite> findByBoiteChirurgicaleIdOrderByDateMouvementAsc(Long boiteId);

    List<MouvementBoite> findByProcessusSterilisationIdOrderByDateMouvementAsc(Long processusId);
}