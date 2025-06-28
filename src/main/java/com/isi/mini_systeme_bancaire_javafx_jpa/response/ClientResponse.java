package com.isi.mini_systeme_bancaire_javafx_jpa.response;

import java.time.LocalDate;

public record ClientResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String adresse,
        LocalDate dateInscription,
        String statut,
        int nombreComptes
) {}