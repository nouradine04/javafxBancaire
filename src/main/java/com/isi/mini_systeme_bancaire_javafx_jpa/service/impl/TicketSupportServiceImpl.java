package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.TicketSupportMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TicketSupportRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.TicketSupportRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.TicketSupportResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.TicketSupportService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketSupportServiceImpl implements TicketSupportService {

    private final TicketSupportRepository ticketRepository;
    private final AdminRepository adminRepository;
    private final ClientService clientService;

    public TicketSupportServiceImpl() {
        this.ticketRepository = new TicketSupportRepository();
        this.adminRepository = new AdminRepository();
        this.clientService = new ClientServiceImpl();
    }

    @Override
    public List<TicketSupportResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(TicketSupportMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TicketSupportResponse> getTicketById(Long id) {
        return ticketRepository.findById(id)
                .map(TicketSupportMapper::toResponse);
    }

    @Override
    public List<TicketSupportResponse> getTicketsByClientId(Long clientId) {
        return ticketRepository.findByClientId(clientId).stream()
                .map(TicketSupportMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketSupportResponse> searchTickets(String searchTerm) {
        return ticketRepository.searchTickets(searchTerm).stream()
                .map(TicketSupportMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TicketSupportResponse createTicket(TicketSupportRequest ticketRequest) {
        // Vérifier si le client existe
        Optional<Client> clientOpt = clientService.getClientEntityById(ticketRequest.clientId());
        if (clientOpt.isEmpty()) {
            Notification.notifWarning("Erreur de création", "Le client spécifié n'existe pas");
            return null;
        }

        Client client = clientOpt.get();

        // Récupérer l'admin si spécifié
        Admin admin = null;
        if (ticketRequest.adminId() != null) {
            admin = adminRepository.findById(ticketRequest.adminId()).orElse(null);
        }

        // Créer un nouveau ticket
        TicketSupport ticket = TicketSupportMapper.fromRequest(ticketRequest, client, admin);

        // Définir la date d'ouverture si non fournie
        if (ticket.getDateOuverture() == null) {
            ticket.setDateOuverture(LocalDateTime.now());
        }

        // Définir le statut si non fourni
        if (ticket.getStatut() == null || ticket.getStatut().isEmpty()) {
            ticket.setStatut("Ouvert");
        }

        // Sauvegarder le ticket
        ticket = ticketRepository.save(ticket);

        // Envoyer un email de confirmation
        try {
            Email.sendTicketCreationNotification(
                    client.getEmail(),
                    client.getNom() + " " + client.getPrenom(),
                    ticket.getId().toString(),
                    ticket.getSujet()
            );
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
        }

        return TicketSupportMapper.toResponse(ticket);
    }

    @Override
    public Optional<TicketSupportResponse> updateTicket(Long id, TicketSupportRequest ticketRequest) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    // Récupérer l'admin si spécifié
                    Admin admin = null;
                    if (ticketRequest.adminId() != null) {
                        admin = adminRepository.findById(ticketRequest.adminId()).orElse(null);
                    }

                    // Sauvegarder le statut actuel pour vérifier s'il a changé
                    String oldStatus = ticket.getStatut();

                    // Mettre à jour les champs du ticket
                    TicketSupportMapper.updateFromRequest(ticket, ticketRequest, admin);

                    // Sauvegarder les modifications
                    TicketSupport updatedTicket = ticketRepository.save(ticket);

                    // Notifier le client si le statut a changé
                    if (ticketRequest.statut() != null && !ticketRequest.statut().equals(oldStatus)) {
                        notifierChangementStatut(updatedTicket);
                    }

                    return TicketSupportMapper.toResponse(updatedTicket);
                });
    }

    private void notifierChangementStatut(TicketSupport ticket) {
        if (ticket.getClient() != null && ticket.getClient().getEmail() != null) {
            try {
                Email.sendTicketStatusNotification(
                        ticket.getClient().getEmail(),
                        ticket.getClient().getNom() + " " + ticket.getClient().getPrenom(),
                        ticket.getId().toString(),
                        ticket.getStatut()
                );
            } catch (Exception e) {
                Notification.notifWarning("Email", "Impossible d'envoyer l'email de notification: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean deleteTicket(Long id) {
        try {
            ticketRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le ticket: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean assignerTicket(Long ticketId, Long adminId) {
        Optional<TicketSupport> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            Notification.notifError("Erreur", "Ticket introuvable");
            return false;
        }

        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            Notification.notifError("Erreur", "Administrateur introuvable");
            return false;
        }

        TicketSupport ticket = ticketOpt.get();
        Admin admin = adminOpt.get();

        // Assigner l'admin et mettre à jour le statut
        ticket.assignerAdmin(admin);

        // Sauvegarder les modifications
        ticketRepository.save(ticket);

        // Notifier le client
        notifierChangementStatut(ticket);

        return true;
    }

    @Override
    public boolean cloturerTicket(Long ticketId, String commentaire) {
        Optional<TicketSupport> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            Notification.notifError("Erreur", "Ticket introuvable");
            return false;
        }

        TicketSupport ticket = ticketOpt.get();

        // Ajouter le commentaire si fourni
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            ticket.ajouterCommentaire(commentaire);
        }

        // Résoudre le ticket
        ticket.resoudreTicket(commentaire);

        // Sauvegarder les modifications
        ticketRepository.save(ticket);

        // Notifier le client
        notifierChangementStatut(ticket);

        return true;
    }

    @Override
    public int getNombreTicketsOuverts() {
        return ticketRepository.countTicketsByStatut("Ouvert") +
                ticketRepository.countTicketsByStatut("En cours");
    }

    @Override
    public int getNombreTicketsResolus() {
        return ticketRepository.countTicketsByStatut("Résolu") +
                ticketRepository.countTicketsByStatut("Fermé");
    }

    @Override
    public double getTempsResolutionMoyen() {
        return ticketRepository.calculateAverageResolutionTime();
    }
}