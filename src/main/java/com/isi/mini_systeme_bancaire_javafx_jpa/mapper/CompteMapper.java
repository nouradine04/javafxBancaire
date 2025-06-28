package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;


import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;

import java.time.LocalDate;

public class CompteMapper {

    public static Compte fromRequest(CompteRequest request, Client client) {
        if (request == null) {
            return null;
        }

        Compte compte = new Compte();
        compte.setNumero(request.numero());
        compte.setType(request.type());
        compte.setSolde(request.solde());
        compte.setDateCreation(request.dateCreation() != null ? request.dateCreation() : LocalDate.now());
        compte.setStatut(request.statut());
        compte.setClient(client);

        return compte;
    }

    public static CompteResponse toResponse(Compte compte) {
        if (compte == null) {
            return null;
        }

        return new CompteResponse(
                compte.getId(),
                compte.getNumero(),
                compte.getType(),
                compte.getSolde(),
                compte.getDateCreation(),
                compte.getStatut(),
                compte.getClient() != null ? compte.getClient().getNom() : "",
                compte.getClient() != null ? compte.getClient().getPrenom() : "",
                compte.getCarte() != null
        );
    }

    public static void updateFromRequest(Compte compte, CompteRequest request) {
        if (compte == null || request == null) {
            return;
        }

        if (request.type() != null) {
            compte.setType(request.type());
        }

        if (request.statut() != null) {
            compte.setStatut(request.statut());
        }
    }
}