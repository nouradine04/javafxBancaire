package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TicketSupportRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.TicketSupportRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.TicketSupportServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.TicketSupportService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientTicketController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private TextField txtSujet;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Button btnEnvoyer;

    @FXML
    private Button btnClear;

    @FXML
    private TableView<TicketSupport> tableTickets;

    @FXML
    private TableColumn<TicketSupport, Long> colId;

    @FXML
    private TableColumn<TicketSupport, String> colSujet;

    @FXML
    private TableColumn<TicketSupport, LocalDateTime> colDate;

    @FXML
    private TableColumn<TicketSupport, String> colStatut;

    @FXML
    private TableColumn<TicketSupport, String> colAdmin;

    @FXML
    private Label lblStatutTicket;

    @FXML
    private TextArea txtDetailsTicket;

    private TicketSupportRepository ticketRepository = new TicketSupportRepository();
    private TicketSupportService ticketService = new TicketSupportServiceImpl();

    private ObservableList<TicketSupport> ticketsList = FXCollections.observableArrayList();

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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateOuverture"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colAdmin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAdmin() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getAdmin().getUsername()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        // Charger les tickets du client
        chargerTickets();
    }

    private void chargerTickets() {
        try {
            // Récupérer tous les tickets du client connecté
            List<TicketSupport> tickets = ticketRepository.findByClientId(SessionManager.getCurrentClient().getId());

            // Mettre à jour la liste observable
            ticketsList.clear();
            ticketsList.addAll(tickets);

            // Mettre à jour le tableau
            tableTickets.setItems(ticketsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des tickets : " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectionTicket() {
        // Récupérer le ticket sélectionné
        TicketSupport ticket = tableTickets.getSelectionModel().getSelectedItem();

        if (ticket != null) {
            // Afficher les détails du ticket
            lblStatutTicket.setText("Statut: " + ticket.getStatut());

            String details = "Ticket #" + ticket.getId() + "\n"
                    + "Sujet: " + ticket.getSujet() + "\n"
                    + "Date d'ouverture: " + formatDateTime(ticket.getDateOuverture()) + "\n"
                    + "Statut: " + ticket.getStatut() + "\n";

            if (ticket.getAdmin() != null) {
                details += "Agent en charge: " + ticket.getAdmin().getUsername() + "\n";
            }

            details += "\nDescription:\n" + ticket.getDescription();

            txtDetailsTicket.setText(details);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    @FXML
    private void handleEnvoyer(ActionEvent event) {
        try {
            // Vérifier que tous les champs sont remplis
            if (txtSujet.getText().isEmpty() || txtDescription.getText().isEmpty()) {
                Notification.notifWarning("Ticket de support", "Veuillez remplir tous les champs");
                return;
            }

            // Récupérer les valeurs
            String sujet = txtSujet.getText();
            String description = txtDescription.getText();

            // Créer un ticket
            TicketSupportRequest ticketRequest = new TicketSupportRequest(
                    sujet,
                    description,
                    LocalDateTime.now(),
                    "Ouvert",
                    SessionManager.getCurrentClient().getId(),
                    null
            );

            // Sauvegarder le ticket
            ticketService.createTicket(ticketRequest);

            // Afficher un message de succès
            Notification.notifSuccess("Ticket de support", "Votre ticket a été envoyé avec succès");

            // Réinitialiser les champs
            txtSujet.clear();
            txtDescription.clear();

            // Recharger les tickets
            chargerTickets();
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'envoi du ticket : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        txtSujet.clear();
        txtDescription.clear();
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