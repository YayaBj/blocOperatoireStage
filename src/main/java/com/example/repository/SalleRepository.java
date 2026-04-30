package com.example.repository;

import com.example.entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalleRepository extends JpaRepository<Salle, Long>, JpaSpecificationExecutor<Salle> {
}