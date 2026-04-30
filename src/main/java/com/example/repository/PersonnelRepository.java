package com.example.repository;

import com.example.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonnelRepository extends JpaRepository<Personnel, Long>, JpaSpecificationExecutor<Personnel> {
}