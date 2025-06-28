package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.FraisBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.FraisBancaireRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.FraisBancaireResponse;

import java.time.LocalDate;

public class FraisBancaireMapper {

    public static FraisBancaire fromRequest(FraisBancaireRequest request, Compte compte) {
        if (request == null) {
            return null;
        }

        FraisBancaire frais = new FraisBancaire();
        frais.setType(request.type());
        frais.setMontant(request.montant());
        frais.setDateApplication(request.dateApplication() != null ? request.dateApplication() : LocalDate.now());
        frais.setCompte(compte);

        return frais;
    }

    public static FraisBancaireResponse toResponse(FraisBancaire frais) {
        if (frais == null) {
            return null;
        }

        return new FraisBancaireResponse(
                frais.getId(),
                frais.getType(),
                frais.getMontant(),
                frais.getDateApplication(),
                frais.getCompte() != null ? frais.getCompte().getNumero() : "",
                frais.getCompte() != null && frais.getCompte().getClient() != null ? frais.getCompte().getClient().getNom() : "",
                frais.getCompte() != null && frais.getCompte().getClient() != null ? frais.getCompte().getClient().getPrenom() : "",
                frais.getCompte() != null ? frais.getCompte().getId() : null,
                frais.getCompte() != null && frais.getCompte().getClient() != null ? frais.getCompte().getClient().getId() : null
        );
    }

    public static void updateFromRequest(FraisBancaire frais, FraisBancaireRequest request) {
        if (frais == null || request == null) {
            return;
        }

        if (request.type() != null) {
            frais.setType(request.type());
        }

        if (request.montant() > 0) {
            frais.setMontant(request.montant());
        }

        if (request.dateApplication() != null) {
            frais.setDateApplication(request.dateApplication());
        }
    }
}