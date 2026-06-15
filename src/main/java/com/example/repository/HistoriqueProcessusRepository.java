package com.example.repository;

import com.example.entity.HistoriqueProcessus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueProcessusRepository extends JpaRepository<HistoriqueProcessus, Long> {

    List<HistoriqueProcessus> findByProcessusIdOrderByDateActionAsc(Long processusId);
}