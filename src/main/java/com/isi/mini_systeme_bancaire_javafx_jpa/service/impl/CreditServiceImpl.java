package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.CreditMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.RemboursementRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CreditRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CreditResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CreditService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final RemboursementRepository remboursementRepository;
    private final ClientService clientService;

    public CreditServiceImpl() {
        this.creditRepository = new CreditRepository();
        this.remboursementRepository = new RemboursementRepository();
        this.clientService = new ClientServiceImpl();
    }

    @Override
    public List<CreditResponse> getAllCredits() {
        return creditRepository.findAll().stream()
                .map(CreditMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CreditResponse> getCreditById(Long id) {
        return creditRepository.findById(id)
                .map(CreditMapper::toResponse);
    }

    @Override
    public List<CreditResponse> getCreditsByClientId(Long clientId) {
        return creditRepository.findByClientId(clientId).stream()
                .map(CreditMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditResponse> searchCredits(String searchTerm) {
        return creditRepository.searchCredits(searchTerm).stream()
                .map(CreditMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CreditResponse createCredit(CreditRequest creditRequest) {
        // Vérifier si le client existe
        Optional<Client> clientOpt = clientService.getClientEntityById(creditRequest.clientId());
        if (clientOpt.isEmpty()) {
            Notification.notifWarning("Erreur de création", "Le client spécifié n'existe pas");
            return null;
        }

        Client client = clientOpt.get();

        // Créer un nouveau crédit
        Credit credit = CreditMapper.fromRequest(creditRequest, client);

        // Définir la date de demande si non fournie
        if (credit.getDateDemande() == null) {
            credit.setDateDemande(LocalDate.now());
        }

        // Si la mensualité n'est pas définie, la calculer
        if (credit.getMensualite() <= 0) {
            credit.setMensualite(calculerMensualite(
                    credit.getMontant(),
                    credit.getTauxInteret(),
                    credit.getDureeEnMois()
            ));
        }

        // Définir le statut si non fourni
        if (credit.getStatut() == null || credit.getStatut().isEmpty()) {
            credit.setStatut("En attente");
        }

        // Sauvegarder le crédit
        credit = creditRepository.save(credit);

        // Envoyer un email de confirmation
        try {
            String message = "Votre demande de crédit a été enregistrée avec succès. "
                    + "Montant: " + credit.getMontant() + " FCFA, "
                    + "Durée: " + credit.getDureeEnMois() + " mois, "
                    + "Mensualité: " + credit.getMensualite() + " FCFA. "
                    + "Nous vous contacterons prochainement pour la suite.";

            Email.sendCustomEmail(
                    client.getEmail(),
                    "Confirmation de demande de crédit",
                    message
            );
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
        }

        return CreditMapper.toResponse(credit);
    }

    @Override
    public Optional<CreditResponse> updateCredit(Long id, CreditRequest creditRequest) {
        return creditRepository.findById(id)
                .map(credit -> {
                    // Mettre à jour les champs du crédit
                    CreditMapper.updateFromRequest(credit, creditRequest);

                    // Sauvegarder les modifications
                    Credit updatedCredit = creditRepository.save(credit);

                    // Notifier le client si le statut a changé
                    if (creditRequest.statut() != null && !creditRequest.statut().equals(credit.getStatut())) {
                        notifierChangementStatut(updatedCredit);
                    }

                    return CreditMapper.toResponse(updatedCredit);
                });
    }

    private void notifierChangementStatut(Credit credit) {
        if (credit.getClient() != null && credit.getClient().getEmail() != null) {
            try {
                String message = "Le statut de votre demande de crédit a été mis à jour : "
                        + credit.getStatut() + ".\n"
                        + "Montant: " + credit.getMontant() + " FCFA, "
                        + "Durée: " + credit.getDureeEnMois() + " mois, "
                        + "Mensualité: " + credit.getMensualite() + " FCFA.";

                Email.sendCustomEmail(
                        credit.getClient().getEmail(),
                        "Mise à jour de votre demande de crédit",
                        message
                );
            } catch (Exception e) {
                Notification.notifWarning("Email", "Impossible d'envoyer l'email de notification: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean deleteCredit(Long id) {
        try {
            creditRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le crédit: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double calculerMensualite(double montant, double tauxAnnuel, int dureeEnMois) {
        double tauxMensuel = tauxAnnuel / 12 / 100;
        double facteur = Math.pow(1 + tauxMensuel, dureeEnMois);
        return Math.round((montant * tauxMensuel * facteur / (facteur - 1)) * 100) / 100.0;
    }

    @Override
    public double calculerMontantTotal(double mensualite, int dureeEnMois) {
        return Math.round((mensualite * dureeEnMois) * 100) / 100.0;
    }

    @Override
    public double calculerTauxEffectifGlobal(double montant, double mensualite, int dureeEnMois) {
        // Méthode de Newton-Raphson pour approximer le TEG
        double montantTotal = mensualite * dureeEnMois;
        double tauxInitial = 0.05; // 5% comme point de départ
        double precision = 0.0000001;
        double taux = tauxInitial;

        for (int i = 0; i < 100; i++) { // Maximum 100 itérations
            double f = 0;
            double df = 0;

            for (int m = 1; m <= dureeEnMois; m++) {
                double v = Math.pow(1 + taux, -m);
                f += mensualite * v;
                df += -m * mensualite * Math.pow(1 + taux, -m - 1);
            }

            f -= montant;

            double delta = f / df;
            taux -= delta;

            if (Math.abs(delta) < precision) {
                break;
            }
        }

        // Convertir le taux mensuel en taux annuel effectif global
        double tauxAnnuel = Math.pow(1 + taux, 12) - 1;
        return Math.round(tauxAnnuel * 10000) / 100.0; // Retourne le pourcentage avec 2 décimales
    }

    @Override
    public boolean effectuerRemboursement(Long creditId, double montant, LocalDate date) {
        Optional<Credit> creditOpt = creditRepository.findById(creditId);
        if (creditOpt.isEmpty()) {
            Notification.notifError("Erreur", "Crédit introuvable");
            return false;
        }

        Credit credit = creditOpt.get();

        // Vérifier le statut du crédit
        if (!"Approuvé".equals(credit.getStatut()) && !"En cours".equals(credit.getStatut())) {
            Notification.notifError("Erreur", "Le crédit n'est pas en cours de remboursement");
            return false;
        }

        try {
            // Créer un nouveau remboursement
            Remboursement remboursement = new Remboursement(montant, date != null ? date : LocalDate.now());
            remboursement.setCredit(credit);

            // Sauvegarder le remboursement
            remboursementRepository.save(remboursement);

            // Mettre à jour le statut du crédit si nécessaire
            updateCreditStatusAfterRemboursement(credit);

            // Notifier le client
            notifierRemboursement(credit, montant);

            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur", "Impossible d'effectuer le remboursement: " + e.getMessage());
            return false;
        }
    }

    private void updateCreditStatusAfterRemboursement(Credit credit) {
        // Calculer le montant total à rembourser
        double montantTotal = calculerMontantTotal(credit.getMensualite(), credit.getDureeEnMois());

        // Calculer le montant déjà remboursé
        double montantRembourse = credit.getRemboursements().stream()
                .mapToDouble(Remboursement::getMontant)
                .sum();

        // Si tout est remboursé, mettre à jour le statut
        if (montantRembourse >= montantTotal) {
            credit.setStatut("Remboursé");
            creditRepository.save(credit);

            // Envoyer un email de confirmation
            if (credit.getClient() != null && credit.getClient().getEmail() != null) {
                try {
                    String message = "Félicitations ! Votre crédit a été entièrement remboursé. "
                            + "Merci pour votre confiance.";

                    Email.sendCustomEmail(
                            credit.getClient().getEmail(),
                            "Crédit entièrement remboursé",
                            message
                    );
                } catch (Exception e) {
                    // Ignore l'erreur d'envoi d'email
                }
            }
        } else if (!"En cours".equals(credit.getStatut()) && "Approuvé".equals(credit.getStatut())) {
            credit.setStatut("En cours");
            creditRepository.save(credit);
        }
    }

    private void notifierRemboursement(Credit credit, double montant) {
        if (credit.getClient() != null && credit.getClient().getEmail() != null) {
            try {
                double montantTotal = calculerMontantTotal(credit.getMensualite(), credit.getDureeEnMois());
                double montantRembourse = credit.getRemboursements().stream()
                        .mapToDouble(Remboursement::getMontant)
                        .sum();
                double montantRestant = montantTotal - montantRembourse;

                String message = "Un remboursement de " + montant + " FCFA a été effectué sur votre crédit. "
                        + "Montant restant à rembourser: " + montantRestant + " FCFA.";

                Email.sendCustomEmail(
                        credit.getClient().getEmail(),
                        "Confirmation de remboursement",
                        message
                );
            } catch (Exception e) {
                // Ignore l'erreur d'envoi d'email
            }
        }
    }

    @Override
    public List<Remboursement> getRemboursementsByCreditId(Long creditId) {
        return remboursementRepository.findByCreditId(creditId);
    }

    @Override
    public CreditResponse simulerCredit(double montant, double tauxAnnuel, int dureeEnMois) {
        double mensualite = calculerMensualite(montant, tauxAnnuel, dureeEnMois);
        double montantTotal = calculerMontantTotal(mensualite, dureeEnMois);
        double coutTotal = montantTotal - montant;
        double teg = calculerTauxEffectifGlobal(montant, mensualite, dureeEnMois);

        Credit simulation = new Credit();
        simulation.setMontant(montant);
        simulation.setTauxInteret(tauxAnnuel);
        simulation.setDureeEnMois(dureeEnMois);
        simulation.setMensualite(mensualite);
        simulation.setDateDemande(LocalDate.now());
        simulation.setStatut("Simulation");

        return new CreditResponse(
                null,
                montant,
                tauxAnnuel,
                dureeEnMois,
                mensualite,
                LocalDate.now(),
                "Simulation",
                null,
                null,
                null,
                montantTotal,
                0
        );
    }
}