package com.example.repository;

import com.example.entity.InterventionMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterventionMaterielRepository extends JpaRepository<InterventionMateriel, Long>, JpaSpecificationExecutor<InterventionMateriel> {
}