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

    @Transactional(readOnly = true)
    public List<HistoriqueProcessus> findByProcessus(Long processusId) {
        return historiqueProcessusRepository.findByProcessusIdOrderByDateActionAsc(processusId);
    }
}