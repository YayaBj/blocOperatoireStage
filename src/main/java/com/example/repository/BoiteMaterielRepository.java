package com.example.repository;

import com.example.entity.BoiteMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoiteMaterielRepository extends JpaRepository<BoiteMateriel, Long> {

    List<BoiteMateriel> findByBoiteChirurgicaleId(Long boiteId);
}