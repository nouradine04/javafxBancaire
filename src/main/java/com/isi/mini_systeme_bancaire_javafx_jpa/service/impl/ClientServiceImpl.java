package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;



import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.ClientMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl() {
        this.clientRepository = new ClientRepository();
    }

    @Override
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientResponse> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(ClientMapper::toResponse);
    }

    @Override
    public Optional<ClientResponse> getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(ClientMapper::toResponse);
    }

    @Override
    public List<ClientResponse> searchClients(String searchTerm) {
        return clientRepository.searchClients(searchTerm).stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponse createClient(ClientRequest clientRequest) {
        // Vérifier si l'email est déjà utilisé
        if (clientRepository.findByEmail(clientRequest.email()).isPresent()) {
            Notification.notifWarning("Erreur de création", "Un client avec cet email existe déjà");
            return null;
        }

        // Créer un nouveau client
        Client client = ClientMapper.fromRequest(clientRequest);

        // Définir la date d'inscription et le statut si non fournis
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }

        if (client.getStatut() == null || client.getStatut().isEmpty()) {
            client.setStatut("actif");
        }

        // Sauvegarder le client
        client = clientRepository.save(client);

        // Envoyer un email de bienvenue
        try {
            Email.sendWelcomeEmail(client.getEmail(), client.getNom() + " " + client.getPrenom(), "");
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de bienvenue: " + e.getMessage());
        }

        return ClientMapper.toResponse(client);
    }

    @Override
    public Optional<ClientResponse> updateClient(Long id, ClientRequest clientRequest) {
        return clientRepository.findById(id)
                .map(client -> {
                    // Mettre à jour les champs du client
                    ClientMapper.updateFromRequest(client, clientRequest);

                    // Sauvegarder les modifications
                    Client updatedClient = clientRepository.save(client);
                    return ClientMapper.toResponse(updatedClient);
                });
    }

    @Override
    public boolean deleteClient(Long id) {
        try {
            clientRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le client: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Client> getClientEntityById(Long id) {
        return clientRepository.findById(id);
    }
}