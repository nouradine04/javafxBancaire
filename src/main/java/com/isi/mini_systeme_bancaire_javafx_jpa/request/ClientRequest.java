package com.isi.mini_systeme_bancaire_javafx_jpa.request;


import java.time.LocalDate;


public record ClientRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        String adresse,
        LocalDate dateInscription,
        String statut
) {}