package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "frais_bancaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"compte"})
public class FraisBancaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id")
    private Compte compte;

    private String type; // tenue de compte, retrait, virement, etc.
    private double montant;
    private LocalDate dateApplication;

    // Constructeur avec champs principaux
    public FraisBancaire(String type, double montant, LocalDate dateApplication) {
        this.type = type;
        this.montant = montant;
        this.dateApplication = dateApplication;
    }
}