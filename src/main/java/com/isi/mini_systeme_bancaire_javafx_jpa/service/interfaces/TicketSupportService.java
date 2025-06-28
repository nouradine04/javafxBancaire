package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.TicketSupportRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.TicketSupportResponse;

import java.util.List;
import java.util.Optional;

public interface TicketSupportService {

    List<TicketSupportResponse> getAllTickets();

    Optional<TicketSupportResponse> getTicketById(Long id);

    List<TicketSupportResponse> getTicketsByClientId(Long clientId);

    List<TicketSupportResponse> searchTickets(String searchTerm);

    TicketSupportResponse createTicket(TicketSupportRequest ticketRequest);

    Optional<TicketSupportResponse> updateTicket(Long id, TicketSupportRequest ticketRequest);

    boolean deleteTicket(Long id);

    // Méthodes spécifiques aux tickets
    boolean assignerTicket(Long ticketId, Long adminId);

    boolean cloturerTicket(Long ticketId, String commentaire);

    // Méthodes d'analyse
    int getNombreTicketsOuverts();

    int getNombreTicketsResolus();

    double getTempsResolutionMoyen(); // en heures
}