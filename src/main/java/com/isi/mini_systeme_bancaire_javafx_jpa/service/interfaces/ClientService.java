package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;


import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    List<ClientResponse> getAllClients();

    Optional<ClientResponse> getClientById(Long id);

    Optional<ClientResponse> getClientByEmail(String email);

    List<ClientResponse> searchClients(String searchTerm);

    ClientResponse createClient(ClientRequest clientRequest);

    Optional<ClientResponse> updateClient(Long id, ClientRequest clientRequest);

    boolean deleteClient(Long id);

    // Méthode utilitaire pour accéder directement au modèle Client (pour les autres services)
    Optional<Client> getClientEntityById(Long id);
}