package com.isi.mini_systeme_bancaire_javafx_jpa.response;

import java.time.LocalDate;

public record FraisBancaireResponse(
        Long id,
        String type,
        double montant,
        LocalDate dateApplication,
        String numeroCompte,
        String nomClient,
        String prenomClient,
        Long compteId,
        Long clientId
) {}