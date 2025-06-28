package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ClientCompteController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private TableView<Compte> tableComptes;

    @FXML
    private TableColumn<Compte, String> colNumero;

    @FXML
    private TableColumn<Compte, String> colType;

    @FXML
    private TableColumn<Compte, Double> colSolde;

    @FXML
    private TableColumn<Compte, LocalDate> colDateCreation;

    @FXML
    private TableColumn<Compte, String> colStatut;

    @FXML
    private Button btnDemander;

    // Repository pour accéder aux données des comptes
    private CompteRepository compteRepository = new CompteRepository();

    // Liste observable pour les comptes
    private ObservableList<Compte> comptesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un client est connecté
        if (!SessionManager.isClientLoggedIn()) {
            Notification.notifError("Erreur", "Aucun client connecté");
            return;
        }

        // Récupérer le client connecté
        Client client = SessionManager.getCurrentClient();

        // Afficher le nom du client
        lblNomClient.setText(client.getNom() + " " + client.getPrenom());

        // Configurer les colonnes du tableau
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Charger les comptes du client
        chargerComptes();
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes du client connecté
            List<Compte> comptes = compteRepository.findByClientId(SessionManager.getCurrentClient().getId());

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour le tableau
            tableComptes.setItems(comptesList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    @FXML
    private void handleDemander(ActionEvent event) {
        try {
            Outils.openNewWindow("Demande de compte", "/fxml/client/compte.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors de l'ouverture de la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/client/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }

    @FXML
    private void handleTransaction(ActionEvent event) {
        try {
            Outils.load(event, "Transactions", "/fxml/client/transaction.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
        }
    }

    @FXML
    private void handleCarte(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des cartes", "/fxml/client/carte.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des cartes : " + e.getMessage());
        }
    }

    @FXML
    private void handleCredit(ActionEvent event) {
        try {
            Outils.load(event, "Crédits", "/fxml/client/credit.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des crédits : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        SessionManager.logout();

        try {
            Outils.load(event, "Connexion", "/fxml/Login.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la page de connexion : " + e.getMessage());
        }
    }
}