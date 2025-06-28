package com.isi.mini_systeme_bancaire_javafx_jpa.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "cartes_bancaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"compte"})
public class CarteBancaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String cvv;
    private LocalDate dateExpiration;
    private String statut;  // active/bloquée
    private String codePin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id")
    private Compte compte;

    // Constructeur avec champs principaux
    public CarteBancaire(String numero, String cvv, LocalDate dateExpiration, String statut, String codePin) {
        this.numero = numero;
        this.cvv = cvv;
        this.dateExpiration = dateExpiration;
        this.statut = statut;
        this.codePin = codePin;
    }

    // Méthodes pour bloquer ou débloquer la carte
    public void bloquerCarte() {
        this.statut = "bloquée";
    }

    public void debloquerCarte() {
        this.statut = "active";
    }
}
