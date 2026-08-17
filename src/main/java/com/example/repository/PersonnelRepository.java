package com.example.repository;

import com.example.entity.Personnel;
import com.example.entity.enums.EtatPersonnel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PersonnelRepository extends JpaRepository<Personnel, Long>, JpaSpecificationExecutor<Personnel> {

    Page<Personnel> findAllBy(Pageable pageable);

    Personnel findByMatricule(String matricule);

    List<Personnel> findByEtat(EtatPersonnel etat);
}