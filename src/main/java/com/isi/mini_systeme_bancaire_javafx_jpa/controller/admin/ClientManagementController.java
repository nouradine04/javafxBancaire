package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.ClientServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientManagementController implements Initializable {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtAdresse;

    @FXML
    private DatePicker dpDateInscription;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    @FXML
    private TableView<Client> tableClients;

    @FXML
    private TableColumn<Client, String> colNom;

    @FXML
    private TableColumn<Client, String> colPrenom;

    @FXML
    private TableColumn<Client, String> colEmail;

    @FXML
    private TableColumn<Client, String> colTelephone;

    @FXML
    private TableColumn<Client, String> colStatut;

    @FXML
    private TableColumn<Client, String> colComptes;

    @FXML
    private TextField txtSearch;

    private ClientService clientService = new ClientServiceImpl();
    private ClientRepository clientRepository = new ClientRepository();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private Client selectedClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les statuts disponibles
        cbStatut.setItems(FXCollections.observableArrayList("actif", "inactif", "bloqué"));

        // Configurer les colonnes du tableau
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colComptes.setCellValueFactory(cellData -> {
            try {
                Client client = cellData.getValue();
                if (client != null && client.getComptes() != null && !client.getComptes().isEmpty()) {
                    // Récupérer les types de comptes et les joindre en une seule chaîne
                    String typesComptes = client.getComptes().stream()
                            .map(compte -> compte.getType())
                            .collect(Collectors.joining(", "));
                    return javafx.beans.binding.Bindings.createStringBinding(() -> typesComptes);
                } else {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> "Aucun compte");
                }
            } catch (Exception e) {
                // En cas d'erreur (LazyInitializationException), afficher un message par défaut
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Non disponible");
            }
        });

        // Désactiver les boutons de modification et suppression au départ
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Ajouter un listener pour la recherche
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherClients(newValue);
        });

        // Charger les clients
        chargerClients();
    }

    private void chargerClients() {
        try {
            // Récupérer tous les clients avec leurs comptes
            List<Client> clients = clientRepository.findAllWithComptes();

            // Mettre à jour la liste observable
            clientsList.clear();
            clientsList.addAll(clients);

            // Mettre à jour le tableau
            tableClients.setItems(clientsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des clients : " + e.getMessage());
        }
    }

    private void rechercherClients(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                chargerClients();
                return;
            }

            // Rechercher les clients avec leurs comptes
            List<Client> clients = clientRepository.searchClientsWithComptes(searchTerm);

            // Mettre à jour la liste observable
            clientsList.clear();
            clientsList.addAll(clients);

            // Mettre à jour le tableau
            tableClients.setItems(clientsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la recherche des clients : " + e.getMessage());
        }
    }

    @FXML
    private void handleClientSelection() {
        // Récupérer le client sélectionné
        selectedClient = tableClients.getSelectionModel().getSelectedItem();

        if (selectedClient != null) {
            // Remplir les champs avec les informations du client
            txtNom.setText(selectedClient.getNom());
            txtPrenom.setText(selectedClient.getPrenom());
            txtEmail.setText(selectedClient.getEmail());
            txtTelephone.setText(selectedClient.getTelephone());
            txtAdresse.setText(selectedClient.getAdresse());
            dpDateInscription.setValue(selectedClient.getDateInscription());
            cbStatut.setValue(selectedClient.getStatut());

            // Activer les boutons de modification et suppression
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Vérifier que tous les champs obligatoires sont remplis
            if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtEmail.getText().isEmpty() || txtTelephone.getText().isEmpty()) {
                Notification.notifWarning("Client", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Créer un client
            ClientRequest clientRequest = new ClientRequest(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    txtEmail.getText(),
                    txtTelephone.getText(),
                    txtAdresse.getText(),
                    dpDateInscription.getValue() != null ? dpDateInscription.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "actif"
            );

            // Enregistrer le client
            ClientResponse clientResponse = clientService.createClient(clientRequest);

            if (clientResponse != null) {
                Notification.notifSuccess("Client", "Client créé avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les clients
                chargerClients();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la création du client : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Vérifier qu'un client est sélectionné
            if (selectedClient == null) {
                Notification.notifWarning("Client", "Veuillez sélectionner un client");
                return;
            }

            // Vérifier que tous les champs obligatoires sont remplis
            if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtEmail.getText().isEmpty() || txtTelephone.getText().isEmpty()) {
                Notification.notifWarning("Client", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Créer un client
            ClientRequest clientRequest = new ClientRequest(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    txtEmail.getText(),
                    txtTelephone.getText(),
                    txtAdresse.getText(),
                    dpDateInscription.getValue() != null ? dpDateInscription.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "actif"
            );

            // Mettre à jour le client
            Optional<ClientResponse> clientResponse = clientService.updateClient(selectedClient.getId(), clientRequest);

            if (clientResponse.isPresent()) {
                Notification.notifSuccess("Client", "Client mis à jour avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les clients
                chargerClients();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour du client : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Vérifier qu'un client est sélectionné
            if (selectedClient == null) {
                Notification.notifWarning("Client", "Veuillez sélectionner un client");
                return;
            }

            // Demander confirmation
            boolean confirm = Notification.confirmDelete();

            if (confirm) {
                // Supprimer le client
                boolean success = clientService.deleteClient(selectedClient.getId());

                if (success) {
                    Notification.notifSuccess("Client", "Client supprimé avec succès");

                    // Réinitialiser les champs
                    handleClear(event);

                    // Recharger les clients
                    chargerClients();
                }
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la suppression du client : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        dpDateInscription.setValue(null);
        cbStatut.setValue(null);

        selectedClient = null;
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(false);

        // Désélectionner la ligne du tableau
        tableClients.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionComptes(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des comptes", "/fxml/admin/compte.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des comptes : " + e.getMessage());
        }
    }
}