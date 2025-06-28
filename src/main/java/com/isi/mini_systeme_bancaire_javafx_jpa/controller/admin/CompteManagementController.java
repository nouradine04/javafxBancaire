package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CompteServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
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
import java.util.regex.Pattern;

public class CompteManagementController implements Initializable {

    @FXML
    private TextField txtNumero;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private TextField txtSolde;

    @FXML
    private DatePicker dpDateCreation;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private ComboBox<Client> cbClient;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

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
    private TableColumn<Compte, String> colClient;

    @FXML
    private TableColumn<Compte, Boolean> colCarte;

    @FXML
    private TextField txtSearch;

    private final CompteService compteService = new CompteServiceImpl();
    private final CompteRepository compteRepository = new CompteRepository();
    private final ClientRepository clientRepository = new ClientRepository();
    private final ObservableList<Compte> comptesList = FXCollections.observableArrayList();
    private final ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private Compte selectedCompte;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les types de compte disponibles
        cbType.setItems(FXCollections.observableArrayList("COURANT", "EPARGNE", "BLOQUE"));

        // Initialiser les statuts disponibles
        cbStatut.setItems(FXCollections.observableArrayList("actif", "inactif", "bloqué"));

        // Configurer le combobox des clients
        configurerComboBoxClients();

        // Configurer les colonnes du tableau
        configurerColonnesTableau();

        // Désactiver les boutons de modification et suppression au départ
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Ajouter un listener pour la recherche
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> rechercherComptes(newValue));

        // Charger les clients et comptes
        chargerClients();
        chargerComptes();
    }

    private void configurerComboBoxClients() {
        cbClient.setConverter(new javafx.util.StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getNom() + " " + client.getPrenom() + " (" + client.getEmail() + ")";
            }

            @Override
            public Client fromString(String s) {
                return null; // Non utilisé
            }
        });
    }

    private void configurerColonnesTableau() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configuration des colonnes de client et de carte avec gestion des erreurs
        colClient.setCellValueFactory(cellData -> {
            try {
                Client client = cellData.getValue().getClient();
                if (client != null) {
                    String nomComplet = client.getNom() + " " + client.getPrenom();
                    return javafx.beans.binding.Bindings.createStringBinding(() -> nomComplet);
                } else {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
                }
            } catch (Exception e) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Erreur");
            }
        });

        colCarte.setCellValueFactory(cellData -> {
            try {
                // Vérifie si le compte a une carte associée
                boolean hasCarte = cellData.getValue().getCarte() != null;
                return javafx.beans.binding.Bindings.createObjectBinding(() -> hasCarte);
            } catch (Exception e) {
                return javafx.beans.binding.Bindings.createObjectBinding(() -> false);
            }
        });
    }

    private void chargerClients() {
        try {
            // Récupérer tous les clients
            List<Client> clients = clientRepository.findAll();

            // Mettre à jour la liste observable
            clientsList.clear();
            clientsList.addAll(clients);

            // Mettre à jour le combobox
            cbClient.setItems(clientsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des clients : " + e.getMessage());
        }
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes avec leurs clients
            List<Compte> comptes = compteRepository.findAllWithClients();

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour le tableau
            tableComptes.setItems(comptesList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void rechercherComptes(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                chargerComptes();
                return;
            }

            // Rechercher les comptes avec leurs clients
            List<Compte> comptes = compteRepository.searchComptesWithClients(searchTerm);

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour le tableau
            tableComptes.setItems(comptesList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la recherche des comptes : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        // Validation des types de compte
        if (cbType.getValue() == null) {
            Notification.notifWarning("Validation", "Veuillez sélectionner un type de compte");
            return false;
        }

        // Validation du solde
        if (txtSolde.getText().isEmpty()) {
            Notification.notifWarning("Validation", "Veuillez saisir un solde");
            return false;
        }

        try {
            double solde = Double.parseDouble(txtSolde.getText());
            if (solde < 0) {
                Notification.notifWarning("Validation", "Le solde ne peut pas être négatif");
                return false;
            }
        } catch (NumberFormatException e) {
            Notification.notifWarning("Validation", "Le solde doit être un nombre valide");
            return false;
        }

        // Validation du client
        if (cbClient.getValue() == null) {
            Notification.notifWarning("Validation", "Veuillez sélectionner un client");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCompteSelection() {
        // Récupérer le compte sélectionné
        selectedCompte = tableComptes.getSelectionModel().getSelectedItem();

        if (selectedCompte != null) {
            // Remplir les champs avec les informations du compte
            txtNumero.setText(selectedCompte.getNumero());
            cbType.setValue(selectedCompte.getType());
            txtSolde.setText(String.valueOf(selectedCompte.getSolde()));
            dpDateCreation.setValue(selectedCompte.getDateCreation());
            cbStatut.setValue(selectedCompte.getStatut());
            cbClient.setValue(selectedCompte.getClient());

            // Activer les boutons de modification et suppression
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            // Créer un compte
            CompteRequest compteRequest = new CompteRequest(
                    txtNumero.getText().isEmpty() ? null : txtNumero.getText(), // Si vide, le service génère un numéro
                    cbType.getValue(),
                    Double.parseDouble(txtSolde.getText()),
                    dpDateCreation.getValue() != null ? dpDateCreation.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "actif",
                    cbClient.getValue().getId()
            );

            // Enregistrer le compte
            CompteResponse compteResponse = compteService.createCompte(compteRequest);

            if (compteResponse != null) {
                Notification.notifSuccess("Compte", "Compte créé avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les comptes
                chargerComptes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la création du compte : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            // Vérifier qu'un compte est sélectionné
            if (selectedCompte == null) {
                Notification.notifWarning("Compte", "Veuillez sélectionner un compte");
                return;
            }

            // Créer un compte
            CompteRequest compteRequest = new CompteRequest(
                    txtNumero.getText(),
                    cbType.getValue(),
                    Double.parseDouble(txtSolde.getText()),
                    dpDateCreation.getValue() != null ? dpDateCreation.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "actif",
                    cbClient.getValue().getId()
            );

            // Mettre à jour le compte
            Optional<CompteResponse> compteResponse = compteService.updateCompte(selectedCompte.getId(), compteRequest);

            if (compteResponse.isPresent()) {
                Notification.notifSuccess("Compte", "Compte mis à jour avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les comptes
                chargerComptes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour du compte : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Vérifier qu'un compte est sélectionné
            if (selectedCompte == null) {
                Notification.notifWarning("Compte", "Veuillez sélectionner un compte");
                return;
            }

            // Demander confirmation
            boolean confirm = Notification.confirmDelete();

            if (confirm) {
                // Supprimer le compte
                boolean success = compteService.deleteCompte(selectedCompte.getId());

                if (success) {
                    Notification.notifSuccess("Compte", "Compte supprimé avec succès");

                    // Réinitialiser les champs
                    handleClear(event);

                    // Recharger les comptes
                    chargerComptes();
                }
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la suppression du compte : " + e.getMessage());
        }
    }
            @FXML
            private void handleClear(ActionEvent event) {
                txtNumero.clear();
                cbType.setValue(null);
                txtSolde.clear();
                dpDateCreation.setValue(null);
                cbStatut.setValue(null);
                cbClient.setValue(null);

                selectedCompte = null;
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnSave.setDisable(false);

                // Désélectionner la ligne du tableau
                tableComptes.getSelectionModel().clearSelection();
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
            private void handleTransaction(ActionEvent event) {
                try {
                    Outils.load(event, "Transactions", "/fxml/admin/transaction.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
                }
            }

            @FXML
            private void handleCreerCarte(ActionEvent event) {
                try {
                    Outils.load(event, "Gestion des cartes bancaires", "/fxml/admin/carte.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des cartes : " + e.getMessage());
                }
            }
        }