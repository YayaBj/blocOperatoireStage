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
        var salle = new Salle(numeroSalle, typeSalle, statutSalle);
        salleRepository.saveAndFlush(salle);
    }

    @Transactional
    public void updateSalle(Salle salle, String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        salle.setNumeroSalle(numeroSalle);
        salle.setTypeSalle(typeSalle);
        salle.setStatutSalle(statutSalle);
        salleRepository.saveAndFlush(salle);
    }

    @Transactional(readOnly = true)
    public List<Salle> findAll() {
        return salleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Salle> findByetat(StatutSalle statutSalle) {
        return salleRepository.findByStatutSalle(statutSalle);
    }

    @Transactional(readOnly = true)
    public Salle getSalleByNumeroSalle(String numeroSalle) {
        return salleRepository.findByNumeroSalle(numeroSalle);
    }

    @Transactional
    public void deleteSalle(Salle salle) {
        salleRepository.delete(salle);
    }
}
