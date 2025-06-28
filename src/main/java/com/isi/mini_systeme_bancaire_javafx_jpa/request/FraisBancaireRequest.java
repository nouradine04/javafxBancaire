package com.isi.mini_systeme_bancaire_javafx_jpa.request;


import java.time.LocalDate;


public record FraisBancaireRequest(
        String type,
        double montant,
        LocalDate dateApplication,
        Long compteId
) {}