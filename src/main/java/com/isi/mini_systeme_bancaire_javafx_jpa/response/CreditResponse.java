package com.isi.mini_systeme_bancaire_javafx_jpa.response;

import java.time.LocalDate;

public record CreditResponse(
        Long id,
        double montant,
        double tauxInteret,
        int dureeEnMois,
        double mensualite,
        LocalDate dateDemande,
        String statut,
        String nomClient,
        String prenomClient,
        Long clientId,
        double montantRestant,
        int nombreRemboursements
) {}