package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.CompteMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompteServiceImpl implements CompteService {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final ClientService clientService;

    public CompteServiceImpl() {
        this.compteRepository = new CompteRepository();
        this.transactionRepository = new TransactionRepository();
        this.clientService = new ClientServiceImpl();
    }

    @Override
    public List<CompteResponse> getAllComptes() {
        return compteRepository.findAll().stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CompteResponse> getCompteById(Long id) {
        return compteRepository.findById(id)
                .map(CompteMapper::toResponse);
    }

    @Override
    public Optional<CompteResponse> getCompteByNumero(String numero) {
        return compteRepository.findByNumero(numero)
                .map(CompteMapper::toResponse);
    }

    @Override
    public List<CompteResponse> getComptesByClientId(Long clientId) {
        return compteRepository.findByClientId(clientId).stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompteResponse> searchComptes(String searchTerm) {
        return compteRepository.searchComptes(searchTerm).stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompteResponse createCompte(CompteRequest compteRequest) {
        // Vérifier si le client existe
        Optional<Client> clientOpt = clientService.getClientEntityById(compteRequest.clientId());
        if (clientOpt.isEmpty()) {
            Notification.notifWarning("Erreur de création", "Le client spécifié n'existe pas");
            return null;
        }

        Client client = clientOpt.get();

        // Générer un numéro de compte unique si non fourni
        String numero = compteRequest.numero();
        if (numero == null || numero.isEmpty()) {
            numero = Outils.generateAccountNumber();
        } else if (compteRepository.findByNumero(numero).isPresent()) {
            Notification.notifWarning("Erreur de création", "Un compte avec ce numéro existe déjà");
            return null;
        }

        // Créer un nouveau compte
        Compte compte = CompteMapper.fromRequest(
                new CompteRequest(
                        numero,
                        compteRequest.type(),
                        compteRequest.solde(),
                        compteRequest.dateCreation(),
                        compteRequest.statut() != null ? compteRequest.statut() : "actif",
                        compteRequest.clientId()
                ),
                client
        );

        // Définir la date de création si non fournie
        if (compte.getDateCreation() == null) {
            compte.setDateCreation(LocalDate.now());
        }

        // Sauvegarder le compte
        compte = compteRepository.save(compte);

        // Envoyer un email de confirmation
        try {
            Email.sendWelcomeEmail(client.getEmail(), client.getNom() + " " + client.getPrenom(), compte.getNumero());
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
        }

        return CompteMapper.toResponse(compte);
    }

    @Override
    public Optional<CompteResponse> updateCompte(Long id, CompteRequest compteRequest) {
        return compteRepository.findById(id)
                .map(compte -> {
                    // Mettre à jour les champs du compte
                    CompteMapper.updateFromRequest(compte, compteRequest);

                    // Sauvegarder les modifications
                    Compte updatedCompte = compteRepository.save(compte);
                    return CompteMapper.toResponse(updatedCompte);
                });
    }

    @Override
    public boolean deleteCompte(Long id) {
        try {
            compteRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le compte: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerDepot(Long compteId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de dépôt", "Le montant doit être supérieur à zéro");
            return false;
        }

        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifError("Erreur de dépôt", "Compte introuvable");
            return false;
        }

        Compte compte = compteOpt.get();

        try {
            // Augmenter le solde du compte
            compte.setSolde(compte.getSolde() + montant);
            compteRepository.save(compte);

            // Créer une transaction
            Transaction transaction = new Transaction(
                    "DEPOT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompte(compte);
            transactionRepository.save(transaction);

            // Envoyer un email de notification
            Client client = compte.getClient();
            if (client != null) {
                Email.sendNotificationTransaction(
                        client.getEmail(),
                        client.getNom() + " " + client.getPrenom(),
                        "dépôt",
                        montant,
                        compte.getNumero()
                );
            }

            Notification.notifSuccess("Dépôt réussi",
                    "Dépôt de " + montant + " FCFA effectué sur le compte " + compte.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de dépôt", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerRetrait(Long compteId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de retrait", "Le montant doit être supérieur à zéro");
            return false;
        }

        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifError("Erreur de retrait", "Compte introuvable");
            return false;
        }

        Compte compte = compteOpt.get();

        if (compte.getSolde() < montant) {
            Notification.notifError("Erreur de retrait", "Solde insuffisant");
            return false;
        }

        try {
            // Diminuer le solde du compte
            compte.setSolde(compte.getSolde() - montant);
            compteRepository.save(compte);

            // Créer une transaction
            Transaction transaction = new Transaction(
                    "RETRAIT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompte(compte);
            transactionRepository.save(transaction);

            // Envoyer un email de notification
            Client client = compte.getClient();
            if (client != null) {
                Email.sendNotificationTransaction(
                        client.getEmail(),
                        client.getNom() + " " + client.getPrenom(),
                        "retrait",
                        montant,
                        compte.getNumero()
                );
            }

            Notification.notifSuccess("Retrait réussi",
                    "Retrait de " + montant + " FCFA effectué sur le compte " + compte.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de retrait", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerVirement(Long compteSourceId, Long compteDestinationId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de virement", "Le montant doit être supérieur à zéro");
            return false;
        }

        if (compteSourceId.equals(compteDestinationId)) {
            Notification.notifError("Erreur de virement", "Impossible de faire un virement vers le même compte");
            return false;
        }

        Optional<Compte> compteSourceOpt = compteRepository.findById(compteSourceId);
        if (compteSourceOpt.isEmpty()) {
            Notification.notifError("Erreur de virement", "Compte source introuvable");
            return false;
        }

        Optional<Compte> compteDestOpt = compteRepository.findById(compteDestinationId);
        if (compteDestOpt.isEmpty()) {
            Notification.notifError("Erreur de virement", "Compte destination introuvable");
            return false;
        }

        Compte compteSource = compteSourceOpt.get();
        Compte compteDest = compteDestOpt.get();

        if (compteSource.getSolde() < montant) {
            Notification.notifError("Erreur de virement", "Solde insuffisant");
            return false;
        }

        try {
            // Diminuer le solde du compte source
            compteSource.setSolde(compteSource.getSolde() - montant);
            compteRepository.save(compteSource);

            // Augmenter le solde du compte destination
            compteDest.setSolde(compteDest.getSolde() + montant);
            compteRepository.save(compteDest);

            // Créer une transaction
            Transaction transaction = new Transaction(
                    "VIREMENT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompteSource(compteSource);
            transaction.setCompteDestination(compteDest);
            transactionRepository.save(transaction);

            // Envoyer des emails de notification
            Client clientSource = compteSource.getClient();
            if (clientSource != null) {
                Email.sendNotificationTransaction(
                        clientSource.getEmail(),
                        clientSource.getNom() + " " + clientSource.getPrenom(),
                        "virement sortant",
                        montant,
                        compteSource.getNumero()
                );
            }

            Client clientDest = compteDest.getClient();
            if (clientDest != null) {
                Email.sendNotificationTransaction(
                        clientDest.getEmail(),
                        clientDest.getNom() + " " + clientDest.getPrenom(),
                        "virement entrant",
                        montant,
                        compteDest.getNumero()
                );
            }

            Notification.notifSuccess("Virement réussi",
                    "Virement de " + montant + " FCFA effectué du compte " +
                            compteSource.getNumero() + " vers le compte " + compteDest.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de virement", e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Compte> getCompteEntityById(Long id) {
        return compteRepository.findById(id);
    }
}


