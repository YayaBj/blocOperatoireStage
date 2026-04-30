package com.example.repository;

import com.example.entity.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaterielRepository extends JpaRepository<Materiel, Long>, JpaSpecificationExecutor<Materiel> {
}