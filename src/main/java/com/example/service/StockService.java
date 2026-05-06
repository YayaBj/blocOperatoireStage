package com.example.service;

import com.example.entity.Materiel;
import com.example.entity.Stock;
import com.example.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void createStock(Materiel materiel, int quantiteTotale, int quantiteDisponible, int seuilAlerte) {
        var stock = new Stock(quantiteTotale, quantiteDisponible, seuilAlerte);
        materiel.setStock(stock);
        stockRepository.saveAndFlush(stock);
    }

    @Transactional
    public void updateStock(Stock stock, int quantiteTotale, int quantiteDisponible, int seuilAlerte) {
        stock.setQuantiteTotale(quantiteTotale);
        stock.setQuantiteDisponible(quantiteDisponible);
        stock.setSeuilAlerte(seuilAlerte);
        stockRepository.saveAndFlush(stock);
    }

    @Transactional(readOnly = true)
    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    @Transactional
    public void deleteStock(Stock stock) {
        stockRepository.delete(stock);
    }
}