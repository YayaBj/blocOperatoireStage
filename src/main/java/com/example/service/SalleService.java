package com.example.service;

import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import com.example.repository.SalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalleService {

    private final SalleRepository salleRepository;

    SalleService(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    @Transactional
    public void createSalle(String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        verifierSalle(numeroSalle, typeSalle, statutSalle);

        String numero = numeroSalle.trim().toUpperCase();

        if (salleRepository.findByNumeroSalle(numero) != null) {
            throw new IllegalArgumentException("Cette salle existe déjà");
        }

        Salle salle = new Salle(
                numero,
                typeSalle.trim(),
                statutSalle
        );

        salleRepository.saveAndFlush(salle);
    }

    @Transactional
    public void updateSalle(Salle salle, String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        verifierSalle(numeroSalle, typeSalle, statutSalle);

        Salle salleDb = salleRepository.findById(salle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        String numero = numeroSalle.trim().toUpperCase();

        Salle existing = salleRepository.findByNumeroSalle(numero);

        if (existing != null && !existing.getId().equals(salleDb.getId())) {
            throw new IllegalArgumentException("Ce numéro de salle est déjà utilisé");
        }

        salleDb.setNumeroSalle(numero);
        salleDb.setTypeSalle(typeSalle.trim());
        salleDb.setStatutSalle(statutSalle);

        salleRepository.saveAndFlush(salleDb);
    }

    private void verifierSalle(String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        if (numeroSalle == null || numeroSalle.trim().isBlank()) {
            throw new IllegalArgumentException("Le numéro de salle est obligatoire");
        }

        if (typeSalle == null || typeSalle.trim().isBlank()) {
            throw new IllegalArgumentException("Le type de salle est obligatoire");
        }

        if (statutSalle == null) {
            throw new IllegalArgumentException("Le statut de salle est obligatoire");
        }
    }

    @Transactional(readOnly = true)
    public List<Salle> findAll() {
        return salleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Salle> findByStatut(StatutSalle statutSalle) {
        return salleRepository.findByStatutSalle(statutSalle);
    }

    @Transactional(readOnly = true)
    public Salle getSalleByNumeroSalle(String numeroSalle) {
        return salleRepository.findByNumeroSalle(numeroSalle);
    }

    @Transactional
    public void deleteSalle(Salle salle) {
        Salle salleDb = salleRepository.findById(salle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        salleRepository.delete(salleDb);
    }
}
