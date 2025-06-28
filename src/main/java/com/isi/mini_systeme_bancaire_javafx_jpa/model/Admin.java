package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tickets"})
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role; // ROLE_ADMIN, ROLE_SUPER_ADMIN

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketSupport> tickets = new ArrayList<>();

    // Constructeur avec champs principaux
    public Admin(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}