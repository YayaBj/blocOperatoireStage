package com.example.service;

import com.example.entity.HistoriqueProcessus;
import com.example.repository.HistoriqueProcessusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoriqueProcessusService {

    private final HistoriqueProcessusRepository historiqueProcessusRepository;

    public HistoriqueProcessusService(HistoriqueProcessusRepository historiqueProcessusRepository) {
        this.historiqueProcessusRepository = historiqueProcessusRepository;
    }

    /**
     * Retourne l'historique d'un processus de stérilisation,
     * trié chronologiquement par date d'action.
     *
     * @param processusId identifiant du processus de stérilisation concerné
     * @return la liste des entrées de l'historique du processus,
     *         classées de la plus ancienne à la plus récente
     */
    @Transactional(readOnly = true)
    public List<HistoriqueProcessus> findByProcessus(Long processusId) {
        return historiqueProcessusRepository.findByProcessusIdOrderByDateActionAsc(processusId);
    }
}