package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.FraisBancaireMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.FraisBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.FraisBancaireRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.FraisBancaireRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.FraisBancaireResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.FraisBancaireService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FraisBancaireServiceImpl implements FraisBancaireService {

    private final FraisBancaireRepository fraisRepository;
    private final CompteRepository compteRepository;

    // Constantes pour les différents types de frais
    private static final double TAUX_TENUE_COMPTE = 500.0; // FCFA par mois
    private static final double TAUX_TRANSACTION_STANDARD = 0.005; // 0.5%
    private static final double TAUX_TRANSACTION_PREMIUM = 0.002; // 0.2%
    private static final double FRAIS_CARTE_STANDARD = 1000.0; // FCFA
    private static final double FRAIS_CARTE_PREMIUM = 2500.0; // FCFA

    public FraisBancaireServiceImpl() {
        this.fraisRepository = new FraisBancaireRepository();
        this.compteRepository = new CompteRepository();
    }

    @Override
    public List<FraisBancaireResponse> getAllFrais() {
        return fraisRepository.findAll().stream()
                .map(FraisBancaireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FraisBancaireResponse> getFraisById(Long id) {
        return fraisRepository.findById(id)
                .map(FraisBancaireMapper::toResponse);
    }

    @Override
    public List<FraisBancaireResponse> getFraisByCompteId(Long compteId) {
        return fraisRepository.findByCompteId(compteId).stream()
                .map(FraisBancaireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FraisBancaireResponse> getFraisByClientId(Long clientId) {
        return fraisRepository.findByClientId(clientId).stream()
                .map(FraisBancaireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FraisBancaireResponse> getFraisByType(String type) {
        return fraisRepository.findByType(type).stream()
                .map(FraisBancaireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FraisBancaireResponse> getFraisByPeriode(LocalDate debut, LocalDate fin) {
        return fraisRepository.findByPeriode(debut, fin).stream()
                .map(FraisBancaireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalFraisByCompteId(Long compteId) {
        return fraisRepository.getTotalFraisByCompteId(compteId);
    }

    @Override
    public double getTotalFraisByClientId(Long clientId) {
        return fraisRepository.getTotalFraisByClientId(clientId);
    }

    @Override
    public FraisBancaireResponse createFrais(FraisBancaireRequest fraisRequest) {
        // Vérifier si le compte existe
        Optional<Compte> compteOpt = compteRepository.findById(fraisRequest.compteId());
        if (compteOpt.isEmpty()) {
            Notification.notifWarning("Erreur de création", "Le compte spécifié n'existe pas");
            return null;
        }

        Compte compte = compteOpt.get();

        // Créer les frais bancaires
        FraisBancaire frais = FraisBancaireMapper.fromRequest(fraisRequest, compte);

        // Définir la date d'application si non fournie
        if (frais.getDateApplication() == null) {
            frais.setDateApplication(LocalDate.now());
        }

        // Appliquer les frais en déduisant du solde du compte
        compte.setSolde(compte.getSolde() - frais.getMontant());
        compteRepository.save(compte);

        // Sauvegarder les frais
        frais = fraisRepository.save(frais);

        // Envoyer notification au client
        notifierClient(frais);

        return FraisBancaireMapper.toResponse(frais);
    }

    @Override
    public Optional<FraisBancaireResponse> updateFrais(Long id, FraisBancaireRequest fraisRequest) {
        return fraisRepository.findById(id)
                .map(frais -> {
                    // Récupérer l'ancien montant pour ajuster le solde du compte
                    double ancienMontant = frais.getMontant();

                    // Mettre à jour les champs des frais
                    FraisBancaireMapper.updateFromRequest(frais, fraisRequest);

                    // Ajuster le solde du compte si le montant a changé
                    if (frais.getMontant() != ancienMontant && frais.getCompte() != null) {
                        Compte compte = frais.getCompte();
                        compte.setSolde(compte.getSolde() + ancienMontant - frais.getMontant());
                        compteRepository.save(compte);
                    }

                    // Sauvegarder les modifications
                    FraisBancaire updatedFrais = fraisRepository.save(frais);

                    return FraisBancaireMapper.toResponse(updatedFrais);
                });
    }

    @Override
    public boolean deleteFrais(Long id) {
        try {
            // Récupérer les frais
            Optional<FraisBancaire> fraisOpt = fraisRepository.findById(id);
            if (fraisOpt.isPresent()) {
                FraisBancaire frais = fraisOpt.get();

                // Rembourser le montant au compte si nécessaire
                if (frais.getCompte() != null) {
                    Compte compte = frais.getCompte();
                    compte.setSolde(compte.getSolde() + frais.getMontant());
                    compteRepository.save(compte);
                }

                // Supprimer les frais
                fraisRepository.deleteById(id);
            } else {
                return false;
            }

            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer les frais: " + e.getMessage());
            return false;
        }
    }

    @Override
    public FraisBancaireResponse appliquerFraisTenueCompte(Long compteId) {
        // Vérifier si le compte existe
        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
            return null;
        }

        Compte compte = compteOpt.get();

        // Créer les frais de tenue de compte
        FraisBancaireRequest request = new FraisBancaireRequest(
                "Tenue de compte",
                TAUX_TENUE_COMPTE,
                LocalDate.now(),
                compteId
        );

        return createFrais(request);
    }

    @Override
    public FraisBancaireResponse appliquerFraisTransaction(Long compteId, String typeTransaction, double montantTransaction) {
        // Vérifier si le compte existe
        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
            return null;
        }

        Compte compte = compteOpt.get();

        // Déterminer le taux de frais selon le type de compte (premium/standard)
        double taux = "EPARGNE".equals(compte.getType()) ? TAUX_TRANSACTION_PREMIUM : TAUX_TRANSACTION_STANDARD;

        // Calculer le montant des frais
        double montantFrais = montantTransaction * taux;

        // Créer les frais de transaction
        FraisBancaireRequest request = new FraisBancaireRequest(
                "Transaction " + typeTransaction,
                montantFrais,
                LocalDate.now(),
                compteId
        );

        return createFrais(request);
    }

    @Override
    public FraisBancaireResponse appliquerFraisService(Long compteId, String typeService, double montant) {
        // Vérifier si le compte existe
        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
            return null;
        }

        Compte compte = compteOpt.get();

        // Créer les frais de service
        FraisBancaireRequest request = new FraisBancaireRequest(
                typeService,
                montant,
                LocalDate.now(),
                compteId
        );

        return createFrais(request);
    }

    /**
     * Envoie une notification au client concernant les frais appliqués
     */
    private void notifierClient(FraisBancaire frais) {
        if (frais.getCompte() != null && frais.getCompte().getClient() != null) {
            try {
                Email.sendFraisBancairesNotification(
                        frais.getCompte().getClient().getEmail(),
                        frais.getCompte().getClient().getNom() + " " + frais.getCompte().getClient().getPrenom(),
                        frais.getType(),
                        frais.getMontant(),
                        frais.getCompte().getNumero()
                );
            } catch (Exception e) {
                Notification.notifWarning("Email", "Impossible d'envoyer l'email de notification: " + e.getMessage());
            }
        }
    }
}


