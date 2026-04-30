package com.example.repository;

import com.example.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterventionRepository extends JpaRepository<Intervention, Long>, JpaSpecificationExecutor<Intervention> {
}