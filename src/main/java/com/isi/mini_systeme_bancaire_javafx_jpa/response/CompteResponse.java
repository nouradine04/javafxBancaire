package com.isi.mini_systeme_bancaire_javafx_jpa.response;

import java.time.LocalDate;

public record CompteResponse(
        Long id,
        String numero,
        String type,
        double solde,
        LocalDate dateCreation,
        String statut,
        String nomClient,
        String prenomClient,
        boolean hasCarte
) {}