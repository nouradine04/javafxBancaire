package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;


import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CreditRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CreditResponse;

import java.time.LocalDate;

public class CreditMapper {

    public static Credit fromRequest(CreditRequest request, Client client) {
        if (request == null) {
            return null;
        }

        Credit credit = new Credit();
        credit.setMontant(request.montant());
        credit.setTauxInteret(request.tauxInteret());
        credit.setDureeEnMois(request.dureeEnMois());
        credit.setMensualite(request.mensualite());
        credit.setDateDemande(request.dateDemande() != null ? request.dateDemande() : LocalDate.now());
        credit.setStatut(request.statut());
        credit.setClient(client);

        return credit;
    }

    public static CreditResponse toResponse(Credit credit) {
        if (credit == null) {
            return null;
        }

        // Calculer le montant restant et le nombre de remboursements effectués
        double montantTotal = credit.getMensualite() * credit.getDureeEnMois();
        double montantRembourse = credit.getRemboursements().stream()
                .mapToDouble(Remboursement::getMontant)
                .sum();
        double montantRestant = montantTotal - montantRembourse;

        return new CreditResponse(
                credit.getId(),
                credit.getMontant(),
                credit.getTauxInteret(),
                credit.getDureeEnMois(),
                credit.getMensualite(),
                credit.getDateDemande(),
                credit.getStatut(),
                credit.getClient() != null ? credit.getClient().getNom() : "",
                credit.getClient() != null ? credit.getClient().getPrenom() : "",
                credit.getClient() != null ? credit.getClient().getId() : null,
                montantRestant,
                credit.getRemboursements().size()
        );
    }

    public static void updateFromRequest(Credit credit, CreditRequest request) {
        if (credit == null || request == null) {
            return;
        }

        if (request.tauxInteret() > 0) {
            credit.setTauxInteret(request.tauxInteret());
        }

        if (request.mensualite() > 0) {
            credit.setMensualite(request.mensualite());
        }

        if (request.statut() != null) {
            credit.setStatut(request.statut());
        }
    }
}