package com.isi.mini_systeme_bancaire_javafx_jpa.controller;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsername;


    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtAdresse;

    @FXML
    private ImageView imgLogo;

    private AdminRepository adminRepository = new AdminRepository();
    private ClientRepository clientRepository = new ClientRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Code d'initialisation si nécessaire
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        // Récupérer les identifiants saisis
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Vérifier que les champs ne sont pas vides
        if (username.isEmpty() || password.isEmpty()) {
            Notification.notifWarning("Connexion", "Veuillez remplir tous les champs");
            return;
        }

        // Essayer de connecter en tant qu'administrateur
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(password)) {
                // Connexion réussie en tant qu'administrateur
                SessionManager.setCurrentAdmin(admin);
                try {
                    Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
                }
                return;
            }
        }

        // Essayer de connecter en tant que client
        Optional<Client> clientOpt = clientRepository.findByEmail(username);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            // Dans une vraie application, le mot de passe serait hashé et vérifié de manière sécurisée
            if (client.getTelephone().equals(password)) { // Utilisation du téléphone comme mot de passe pour la démo
                // Connexion réussie en tant que client
                SessionManager.setCurrentClient(client);
                try {
                    Outils.load(event, "Client - Tableau de bord", "/fxml/client/dashboard.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
                }
                return;
            }
        }

        // Si on arrive ici, la connexion a échoué
        Notification.notifError("Connexion", "Identifiants incorrects");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Récupérer les identifiants saisis
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String email = txtEmail.getText();  // Supposons que tu as un champ email
        String telephone = txtTelephone.getText();  // Supposons que tu as un champ téléphone
        String adresse = txtAdresse.getText();  // Supposons que tu as un champ adresse

        // Vérifier que les champs ne sont pas vides
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
            Notification.notifWarning("Inscription", "Veuillez remplir tous les champs");
            return;
        }

        // Vérifier si l'utilisateur existe déjà
        Optional<Client> existingClient = clientRepository.findByEmail(email);
        if (existingClient.isPresent()) {
            Notification.notifWarning("Inscription", "Cet email est déjà utilisé");
            return;
        }

        // Créer un nouveau client avec statut inactif
        Client newClient = new Client(username, "", email, telephone, adresse, LocalDate.now(), "inactif");

        // Enregistrer dans la base de données
        try {
            clientRepository.save(newClient);
            Notification.notifSuccess("Inscription", "Compte créé avec succès ! En attente d'activation.");

            // Rediriger vers la page de connexion
            Outils.load(event, "Connexion", "/fxml/login.fxml");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'inscription : " + e.getMessage());
        }
    }
}

