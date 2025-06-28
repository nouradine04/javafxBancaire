package com.isi.mini_systeme_bancaire_javafx_jpa.request;

import java.time.LocalDateTime;
public record TicketSupportRequest(
        String sujet,
        String description,
        LocalDateTime dateOuverture,
        String statut,
        Long clientId,
        Long adminId
) {}