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
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "transactions", "frais", "carte"})
public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String type;  // courant/épargne
    private double solde;
    private LocalDate dateCreation;
    private String statut;  // validé/rejeté

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FraisBancaire> frais = new ArrayList<>();

    @OneToOne(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CarteBancaire carte;

    // Constructeur avec champs principaux
    public Compte(String numero, String type, double solde, LocalDate dateCreation, String statut) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // Méthodes utilitaires
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setCompte(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setCompte(null);
    }

    public void addFrais(FraisBancaire frais) {
        this.frais.add(frais);
        frais.setCompte(this);
    }

    public void removeFrais(FraisBancaire frais) {
        this.frais.remove(frais);
        frais.setCompte(null);
    }
}