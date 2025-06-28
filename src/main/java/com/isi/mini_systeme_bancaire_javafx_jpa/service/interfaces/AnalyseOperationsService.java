package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface AnalyseOperationsService {

    List<Transaction> detecterTransactionsSuspectes();
    List<Transaction> detecterTransactionsMontantEleve(double seuil);
    List<Transaction> detecterTransactionsMultiples(int nombreMin, int intervalleMins);
    List<Transaction> detecterTransactionsHeuresInhabituelles(int debutHeureNormale, int finHeureNormale);
    Map<Long, List<Transaction>> detecterMotifSmurfing(int nombrePetites, double seuilPetite, double seuilGrosse, int intervalleMins);
    String genererRapportAnalyse(LocalDateTime debut, LocalDateTime fin);
    boolean exporterRapportPDF(LocalDateTime debut, LocalDateTime fin, String filePath);
    boolean exporterRapportExcel(LocalDateTime debut, LocalDateTime fin, String filePath);
}