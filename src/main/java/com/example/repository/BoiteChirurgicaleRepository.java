package com.example.repository;

import com.example.entity.BoiteChirurgicale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoiteChirurgicaleRepository extends JpaRepository<BoiteChirurgicale, Long> {

    boolean existsByCodeBoiteIgnoreCase(String codeBoite);

    BoiteChirurgicale findByCodeBoiteIgnoreCase(String codeBoite);
}