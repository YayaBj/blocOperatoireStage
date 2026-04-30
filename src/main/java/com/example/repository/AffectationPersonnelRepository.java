package com.example.repository;

import com.example.entity.AffectationPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AffectationPersonnelRepository extends JpaRepository<AffectationPersonnel, Long>, JpaSpecificationExecutor<AffectationPersonnel> {
}