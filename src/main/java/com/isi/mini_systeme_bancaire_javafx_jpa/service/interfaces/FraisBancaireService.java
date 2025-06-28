package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.FraisBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.FraisBancaireRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.FraisBancaireResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FraisBancaireService {

    List<FraisBancaireResponse> getAllFrais();

    Optional<FraisBancaireResponse> getFraisById(Long id);

    List<FraisBancaireResponse> getFraisByCompteId(Long compteId);

    List<FraisBancaireResponse> getFraisByClientId(Long clientId);

    List<FraisBancaireResponse> getFraisByType(String type);

    List<FraisBancaireResponse> getFraisByPeriode(LocalDate debut, LocalDate fin);

    double getTotalFraisByCompteId(Long compteId);

    double getTotalFraisByClientId(Long clientId);

    FraisBancaireResponse createFrais(FraisBancaireRequest fraisRequest);

    Optional<FraisBancaireResponse> updateFrais(Long id, FraisBancaireRequest fraisRequest);

    boolean deleteFrais(Long id);

    // Méthodes spécifiques pour appliquer des frais
    FraisBancaireResponse appliquerFraisTenueCompte(Long compteId);

    FraisBancaireResponse appliquerFraisTransaction(Long compteId, String typeTransaction, double montantTransaction);

    FraisBancaireResponse appliquerFraisService(Long compteId, String typeService, double montant);
}