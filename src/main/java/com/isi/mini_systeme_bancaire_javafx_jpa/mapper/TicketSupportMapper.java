package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.TicketSupportRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.TicketSupportResponse;

import java.time.Duration;
import java.time.LocalDateTime;

public class TicketSupportMapper {

    public static TicketSupport fromRequest(TicketSupportRequest request, Client client, Admin admin) {
        if (request == null) {
            return null;
        }

        TicketSupport ticket = new TicketSupport();
        ticket.setSujet(request.sujet());
        ticket.setDescription(request.description());
        ticket.setDateOuverture(request.dateOuverture() != null ? request.dateOuverture() : LocalDateTime.now());
        ticket.setStatut(request.statut() != null ? request.statut() : "Ouvert");
        ticket.setClient(client);
        ticket.setAdmin(admin);

        return ticket;
    }

    public static TicketSupportResponse toResponse(TicketSupport ticket) {
        if (ticket == null) {
            return null;
        }

        // Calculer le temps d'attente
        long tempsAttente = 0;
        if (ticket.getDateFermeture() != null) {
            tempsAttente = Duration.between(ticket.getDateOuverture(), ticket.getDateFermeture()).toHours();
        } else {
            tempsAttente = Duration.between(ticket.getDateOuverture(), LocalDateTime.now()).toHours();
        }

        return new TicketSupportResponse(
                ticket.getId(),
                ticket.getSujet(),
                ticket.getDescription(),
                ticket.getDateOuverture(),
                ticket.getDateFermeture(),
                ticket.getStatut(),
                ticket.getClient() != null ? ticket.getClient().getNom() : "",
                ticket.getClient() != null ? ticket.getClient().getPrenom() : "",
                ticket.getClient() != null ? ticket.getClient().getEmail() : "",
                ticket.getClient() != null ? ticket.getClient().getId() : null,
                ticket.getAdmin() != null ? ticket.getAdmin().getUsername() : "",
                ticket.getAdmin() != null ? ticket.getAdmin().getId() : null,
                tempsAttente
        );
    }

    public static void updateFromRequest(TicketSupport ticket, TicketSupportRequest request, Admin admin) {
        if (ticket == null || request == null) {
            return;
        }

        if (request.sujet() != null) {
            ticket.setSujet(request.sujet());
        }

        if (request.description() != null) {
            ticket.setDescription(request.description());
        }

        if (request.statut() != null) {
            String oldStatus = ticket.getStatut();
            ticket.setStatut(request.statut());

            // Si le ticket est fermé ou résolu, on met à jour la date de fermeture
            if (("Fermé".equals(request.statut()) || "Résolu".equals(request.statut()))
                    && !("Fermé".equals(oldStatus) || "Résolu".equals(oldStatus))) {
                ticket.setDateFermeture(LocalDateTime.now());
            }
        }

        if (admin != null) {
            ticket.setAdmin(admin);
        }
    }
}
