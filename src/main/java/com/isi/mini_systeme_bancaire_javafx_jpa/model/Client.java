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
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"comptes", "tickets"})
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDate dateInscription;
    private String statut;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Compte> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketSupport> tickets = new ArrayList<>();

    // Constructeur avec champs principaux
    public Client(String nom, String prenom, String email, String telephone, String adresse, LocalDate dateInscription, String statut) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateInscription = dateInscription;
        this.statut = statut;
    }

    // Méthodes utilitaires
    public void addCompte(Compte compte) {
        comptes.add(compte);
        compte.setClient(this);
    }

    public void removeCompte(Compte compte) {
        comptes.remove(compte);
        compte.setClient(null);
    }

    public void addTicket(TicketSupport ticket) {
        tickets.add(ticket);
        ticket.setClient(this);
    }

    public void removeTicket(TicketSupport ticket) {
        tickets.remove(ticket);
        ticket.setClient(null);
    }
}