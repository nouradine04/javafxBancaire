package com.isi.mini_systeme_bancaire_javafx_jpa.response;


import java.time.LocalDateTime;

public record TicketSupportResponse(
        Long id,
        String sujet,
        String description,
        LocalDateTime dateOuverture,
        LocalDateTime dateFermeture,
        String statut,
        String nomClient,
        String prenomClient,
        String emailClient,
        Long clientId,
        String usernameAdmin,
        Long adminId,
        long tempsAttente  // en heures, si non résolu = temps depuis l'ouverture
) {}