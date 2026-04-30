package com.example.repository;

import com.example.entity.Sterilisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SterilisationRepository extends JpaRepository<Sterilisation, Long>, JpaSpecificationExecutor<Sterilisation> {
}