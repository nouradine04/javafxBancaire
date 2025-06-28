package com.isi.mini_systeme_bancaire_javafx_jpa.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "remboursements"})
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;
    private double tauxInteret;
    private int dureeEnMois;
    private double mensualite;
    private LocalDate dateDemande;
    private String statut; // en attente, approuvé, refusé, remboursé

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Remboursement> remboursements = new ArrayList<>();

    // Constructeur avec champs principaux
    public Credit(double montant, double tauxInteret, int dureeEnMois, double mensualite, LocalDate dateDemande, String statut) {
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeEnMois = dureeEnMois;
        this.mensualite = mensualite;
        this.dateDemande = dateDemande;
        this.statut = statut;
    }

    // Méthodes utilitaires
    public void addRemboursement(Remboursement remboursement) {
        remboursements.add(remboursement);
        remboursement.setCredit(this);
    }

    public void removeRemboursement(Remboursement remboursement) {
        remboursements.remove(remboursement);
        remboursement.setCredit(null);
    }

    // Méthode de calcul de la mensualité
    public double calculerMensualite() {
        double tauxMensuel = tauxInteret / 12 / 100;
        double facteur = Math.pow(1 + tauxMensuel, dureeEnMois);
        return montant * tauxMensuel * facteur / (facteur - 1);
    }
}