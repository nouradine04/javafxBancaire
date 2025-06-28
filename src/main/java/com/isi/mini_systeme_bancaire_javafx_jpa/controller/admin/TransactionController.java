package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransactionController implements Initializable {
    @FXML
    private TableView<Transaction> tableTransactions;

    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, String> colNumeroCompte;
    @FXML
    private TableColumn<Transaction, String> colClient;
    @FXML
    private TableColumn<Transaction, Double> colMontant;
    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;
    @FXML
    private TableColumn<Transaction, String> colStatut;

    @FXML
    private ComboBox<String> cbFiltreType;
    @FXML
    private ComboBox<String> cbFiltreStatut;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;

    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les filtres
        initialiserFiltres();

        // Configurer les colonnes du tableau
        configurerColonnesTableau();

        // Charger les transactions
        chargerTransactions();

        // Configurer les écouteurs pour les filtres
        configurerEcouteursFiltre();
    }

    private void initialiserFiltres() {
        // Types de transactions
        cbFiltreType.setItems(FXCollections.observableArrayList(
                "Tous", "DEPOT", "RETRAIT", "VIREMENT"
        ));
        cbFiltreType.setValue("Tous");

        // Statuts des transactions
        cbFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Validé", "Rejeté"
        ));
        cbFiltreStatut.setValue("Tous");
    }

    private void configurerEcouteursFiltre() {
        cbFiltreType.setOnAction(event -> filtrerTransactions());
        cbFiltreStatut.setOnAction(event -> filtrerTransactions());
        dpDateDebut.setOnAction(event -> filtrerTransactions());
        dpDateFin.setOnAction(event -> filtrerTransactions());
    }

    private void configurerColonnesTableau() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colNumeroCompte.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            String numero = transaction.getCompte() != null
                    ? transaction.getCompte().getNumero()
                    : (transaction.getCompteSource() != null
                    ? transaction.getCompteSource().getNumero()
                    : "N/A");
            return javafx.beans.binding.Bindings.createStringBinding(() -> numero);
        });

        colClient.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            String client = transaction.getCompte() != null && transaction.getCompte().getClient() != null
                    ? transaction.getCompte().getClient().getNom() + " " + transaction.getCompte().getClient().getPrenom()
                    : (transaction.getCompteSource() != null && transaction.getCompteSource().getClient() != null
                    ? transaction.getCompteSource().getClient().getNom() + " " + transaction.getCompteSource().getClient().getPrenom()
                    : "N/A");
            return javafx.beans.binding.Bindings.createStringBinding(() -> client);
        });

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void chargerTransactions() {
        try {
            // Récupérer toutes les transactions
            List<Transaction> transactions = transactionRepository.findAll();

            // Mettre à jour la liste
            transactionsList.clear();
            transactionsList.addAll(transactions);

            // Mettre à jour le tableau
            tableTransactions.setItems(transactionsList);

        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
        }
    }

    private void filtrerTransactions() {
        try {
            // Récupérer toutes les transactions
            List<Transaction> transactions = transactionRepository.findAll();

            // Filtrer par type
            String typeSelectionne = cbFiltreType.getValue();
            if (!"Tous".equals(typeSelectionne)) {
                transactions = transactions.stream()
                        .filter(t -> t.getType().equals(typeSelectionne))
                        .collect(Collectors.toList());
            }

            // Filtrer par statut
            String statutSelectionne = cbFiltreStatut.getValue();
            if (!"Tous".equals(statutSelectionne)) {
                transactions = transactions.stream()
                        .filter(t -> t.getStatut().equals(statutSelectionne))
                        .collect(Collectors.toList());
            }

            // Filtrer par plage de dates
            LocalDateTime dateDebut = dpDateDebut.getValue() != null
                    ? dpDateDebut.getValue().atStartOfDay()
                    : null;
            LocalDateTime dateFin = dpDateFin.getValue() != null
                    ? dpDateFin.getValue().atTime(23, 59, 59)
                    : null;

            if (dateDebut != null && dateFin != null) {
                transactions = transactions.stream()
                        .filter(t -> !t.getDate().isBefore(dateDebut) && !t.getDate().isAfter(dateFin))
                        .collect(Collectors.toList());
            }

            // Mettre à jour la liste
            transactionsList.clear();
            transactionsList.addAll(transactions);

            // Mettre à jour le tableau
            tableTransactions.setItems(transactionsList);

        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du filtrage des transactions : " + e.getMessage());
        }
    }

    @FXML
    private void handleValiderTransaction() {
        Transaction selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            Notification.notifWarning("Validation", "Veuillez sélectionner une...");
            return;
        }

        // Vérifier si la transaction est déjà validée ou rejetée
        if ("Validé".equals(selectedTransaction.getStatut()) ||
                "Rejeté".equals(selectedTransaction.getStatut())) {
           // Notification.notifWarning("Validation", "Cette transaction a déjà été traitée");
            return;
        }

        // Boîte de confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de validation");
        confirmDialog.setHeaderText("Voulez-vous vraiment valider cette transaction ?");
        confirmDialog.setContentText("Type : " + selectedTransaction.getType() +
                "\nMontant : " + selectedTransaction.getMontant() +
                "\nDate : " + selectedTransaction.getDate());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Mettre à jour le statut
                selectedTransaction.setStatut("Validé");

                // Enregistrer la mise à jour
                transactionRepository.save(selectedTransaction);

                // Rafraîchir la liste
                chargerTransactions();

                Notification.notifSuccess("Validation", "Transaction validée avec succès");
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors de la validation : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRejeterTransaction() {
        Transaction selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            Notification.notifWarning("Rejet", "Veuillez sélectionner une transaction à rejeter");
            return;
        }

        // Vérifier si la transaction est déjà validée ou rejetée
        if ("Validé".equals(selectedTransaction.getStatut()) ||
                "Rejeté".equals(selectedTransaction.getStatut())) {
            Notification.notifWarning("Rejet", "Cette transaction a déjà été traitée");
            return;
        }

        // Boîte de confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de rejet");
        confirmDialog.setHeaderText("Voulez-vous vraiment rejeter cette transaction ?");
        confirmDialog.setContentText("Type : " + selectedTransaction.getType() +
                "\nMontant : " + selectedTransaction.getMontant() +
                "\nDate : " + selectedTransaction.getDate());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Mettre à jour le statut
                selectedTransaction.setStatut("Rejeté");

                // Enregistrer la mise à jour
                transactionRepository.save(selectedTransaction);

                // Rafraîchir la liste
                chargerTransactions();

                Notification.notifSuccess("Rejet", "Transaction rejetée avec succès");
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors du rejet : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }
}