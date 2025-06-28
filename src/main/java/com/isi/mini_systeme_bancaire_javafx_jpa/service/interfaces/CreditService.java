package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CreditRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CreditResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CreditService {

    List<CreditResponse> getAllCredits();

    Optional<CreditResponse> getCreditById(Long id);

    List<CreditResponse> getCreditsByClientId(Long clientId);

    List<CreditResponse> searchCredits(String searchTerm);

    CreditResponse createCredit(CreditRequest creditRequest);

    Optional<CreditResponse> updateCredit(Long id, CreditRequest creditRequest);

    boolean deleteCredit(Long id);

    // Méthodes spécifiques aux crédits
    double calculerMensualite(double montant, double tauxAnnuel, int dureeEnMois);

    double calculerMontantTotal(double mensualite, int dureeEnMois);

    double calculerTauxEffectifGlobal(double montant, double mensualite, int dureeEnMois);

    boolean effectuerRemboursement(Long creditId, double montant, LocalDate date);

    List<Remboursement> getRemboursementsByCreditId(Long creditId);

    // Méthode de simulation
    CreditResponse simulerCredit(double montant, double tauxAnnuel, int dureeEnMois);
}