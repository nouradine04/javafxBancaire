package com.isi.mini_systeme_bancaire_javafx_jpa.controller;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtAdresse;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnRegister;

    private ClientRepository clientRepository = new ClientRepository(); // Instanciation directe comme dans LoginController

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String telephone = txtTelephone.getText();
        String adresse = txtAdresse.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty() || password.isEmpty()) {
            Notification.notifWarning("Inscription", "Veuillez remplir tous les champs");
            return;
        }

        // Vérifier si l'email existe déjà
        Optional<Client> existingClient = clientRepository.findByEmail(email);
        if (existingClient.isPresent()) {
            Notification.notifWarning("Inscription", "Cet email est déjà utilisé");
            return;
        }

        // Hashage du mot de passe (à implémenter selon ta logique)
        String hashedPassword = hashPassword(password);

        // Création du client avec mot de passe hashé
        Client newClient = new Client(username, hashedPassword, email, telephone, adresse, LocalDate.now(), "inactif");

        try {
            clientRepository.save(newClient);
            Notification.notifSuccess("Inscription", "Compte créé avec succès ! En attente d'activation.");
            Outils.load(event, "Connexion", "/fxml/login.fxml");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        // Implémente un hashage sécurisé, par exemple avec BCrypt
        return password; // Remplace cette ligne par une vraie fonction de hashage
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }
}
