package com.isi.mini_systeme_bancaire_javafx_jpa.request;

import java.time.LocalDate;


public record CreditRequest(
        double montant,
        double tauxInteret,
        int dureeEnMois,
        double mensualite,
        LocalDate dateDemande,
        String statut,
        Long clientId
) {}