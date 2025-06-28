package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets_support")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "admin"})
public class TicketSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sujet;

    @Column(length = 2000)  // Pour permettre des descriptions plus longues
    private String description;

    private LocalDateTime dateOuverture;

    private LocalDateTime dateFermeture;  // Ajout du champ pour la date de fermeture

    private String statut; // Ouvert, En cours, Résolu, Fermé

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // Champ pour stocker les commentaires/réponses de l'administrateur
    @Column(length = 2000)
    private String commentaireAdmin;

    // Constructeur avec champs principaux
    public TicketSupport(String sujet, String description, LocalDateTime dateOuverture, String statut) {
        this.sujet = sujet;
        this.description = description;
        this.dateOuverture = dateOuverture != null ? dateOuverture : LocalDateTime.now();
        this.statut = statut != null ? statut : "Ouvert";
    }

    // Méthode pour assigner un admin au ticket
    public void assignerAdmin(Admin admin) {
        this.admin = admin;
        if ("Ouvert".equals(this.statut)) {
            this.statut = "En cours";
        }
    }

    // Méthode pour ajouter un commentaire
    public void ajouterCommentaire(String commentaire) {
        this.commentaireAdmin = commentaire;
    }

    // Méthode pour fermer le ticket
    public void fermerTicket() {
        this.statut = "Fermé";
        this.dateFermeture = LocalDateTime.now();
    }

    // Méthode pour marquer comme résolu
    public void resoudreTicket(String commentaire) {
        this.statut = "Résolu";
        this.dateFermeture = LocalDateTime.now();
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            this.commentaireAdmin = commentaire;
        }
    }
}