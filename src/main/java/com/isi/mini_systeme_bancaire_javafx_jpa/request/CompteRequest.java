package com.isi.mini_systeme_bancaire_javafx_jpa.request;




import java.time.LocalDate;


public record CompteRequest(
        String numero,
        String type,
        double solde,
        LocalDate dateCreation,
        String statut,
        Long clientId
) {

}