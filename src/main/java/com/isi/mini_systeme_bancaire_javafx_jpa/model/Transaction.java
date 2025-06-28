package com.isi.mini_systeme_bancaire_javafx_jpa.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"compte", "compteSource", "compteDestination"})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;  // dépôt, retrait, virement
    private double montant;
    private LocalDateTime date;
    private String statut;  // en cours, complétée, rejetée

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id")
    private Compte compte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_dest_id")
    private Compte compteDestination;

    // Constructeur avec champs principaux
    public Transaction(String type, double montant, LocalDateTime date, String statut) {
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.statut = statut;
    }
}