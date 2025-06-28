package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "remboursements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"credit"})
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id")
    private Credit credit;

    // Constructeur avec champs principaux
    public Remboursement(double montant, LocalDate date) {
        this.montant = montant;
        this.date = date;
    }
}