package com.example.repository;

import com.example.entity.BoiteMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoiteMaterielRepository extends JpaRepository<BoiteMateriel, Long> {

    List<BoiteMateriel> findByBoiteChirurgicaleId(Long boiteId);

    boolean existsByUniteMaterielId(Long uniteId);

    boolean existsByUniteMaterielIdAndBoiteChirurgicaleIdNot(
            Long uniteId,
            Long boiteId
    );

    Optional<BoiteMateriel> findByBoiteChirurgicaleIdAndUniteMaterielId(
            Long boiteId,
            Long uniteMaterielId
    );
}