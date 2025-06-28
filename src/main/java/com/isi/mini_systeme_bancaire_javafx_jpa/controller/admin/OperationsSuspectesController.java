package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.AnalyseOperationsServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.AnalyseOperationsService;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class OperationsSuspectesController implements Initializable {

    @FXML
    private DatePicker dpDateDebut;

    @FXML
    private DatePicker dpDateFin;

    @FXML
    private Button btnAnalyser;

    @FXML
    private Button btnExportPDF;

    @FXML
    private Button btnExportExcel;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabTransactions;

    @FXML
    private TableView<Transaction> tableTransactions;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;

    @FXML
    private TableColumn<Transaction, String> colType;

    @FXML
    private TableColumn<Transaction, Double> colMontant;

    @FXML
    private TableColumn<Transaction, String> colClient;

    @FXML
    private TableColumn<Transaction, String> colCompte;

    @FXML
    private TableColumn<Transaction, String> colStatut;

    @FXML
    private Tab tabRapport;

    @FXML
    private TextArea txtRapport;

    private AnalyseOperationsService analyseService = new AnalyseOperationsServiceImpl();
    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les dates
        dpDateDebut.setValue(LocalDate.now().minusMonths(1));
        dpDateFin.setValue(LocalDate.now());

        // Configurer les colonnes du tableau
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colClient.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null && cellData.getValue().getCompte().getClient() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getClient().getNom() + " " +
                                cellData.getValue().getCompte().getClient().getPrenom()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });
        colCompte.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    @FXML
    private void handleAnalyser(ActionEvent event) {
        try {
            // Vérifier que les dates sont sélectionnées
            if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
                Notification.notifWarning("Analyse", "Veuillez sélectionner une période d'analyse");
                return;
            }

            // Convertir les dates en LocalDateTime
            LocalDateTime debut = dpDateDebut.getValue().atStartOfDay();
            LocalDateTime fin = dpDateFin.getValue().atTime(LocalTime.MAX);

            // Détecter les transactions suspectes
            List<Transaction> transactionsSuspectes = analyseService.detecterTransactionsSuspectes();

            // Filtrer par période
            transactionsSuspectes = transactionsSuspectes.stream()
                    .filter(t -> !t.getDate().isBefore(debut) && !t.getDate().isAfter(fin))
                    .toList();

            // Mettre à jour la liste observable
            transactionsList.clear();
            transactionsList.addAll(transactionsSuspectes);

            // Mettre à jour le tableau
            tableTransactions.setItems(transactionsList);

            // Générer le rapport
            String rapport = analyseService.genererRapportAnalyse(debut, fin);
            txtRapport.setText(rapport);

            Notification.notifSuccess("Analyse", "Analyse effectuée avec succès");

            // Changer l'onglet pour afficher les transactions
            tabPane.getSelectionModel().select(tabTransactions);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'analyse : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        try {
            // Vérifier que l'analyse a été effectuée
            if (transactionsList.isEmpty()) {
                Notification.notifWarning("Export", "Veuillez d'abord effectuer une analyse");
                return;
            }

            // Afficher la boîte de dialogue pour choisir l'emplacement du fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("rapport_operations_suspectes.pdf");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                // Convertir les dates en LocalDateTime
                LocalDateTime debut = dpDateDebut.getValue().atStartOfDay();
                LocalDateTime fin = dpDateFin.getValue().atTime(LocalTime.MAX);

                // Exporter le rapport en PDF
                boolean success = analyseService.exporterRapportPDF(debut, fin, file.getAbsolutePath());

                if (success) {
                    Notification.notifSuccess("Export", "Rapport exporté avec succès");
                } else {
                    Notification.notifError("Erreur", "Erreur lors de l'export du rapport");
                }
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'export : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        try {
            // Vérifier que l'analyse a été effectuée
            if (transactionsList.isEmpty()) {
                Notification.notifWarning("Export", "Veuillez d'abord effectuer une analyse");
                return;
            }

            // Afficher la boîte de dialogue pour choisir l'emplacement du fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            fileChooser.setInitialFileName("rapport_operations_suspectes.xlsx");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                // Convertir les dates en LocalDateTime
                LocalDateTime debut = dpDateDebut.getValue().atStartOfDay();
                LocalDateTime fin = dpDateFin.getValue().atTime(LocalTime.MAX);

                // Exporter le rapport en Excel
                boolean success = analyseService.exporterRapportExcel(debut, fin, file.getAbsolutePath());

                if (success) {
                    Notification.notifSuccess("Export", "Rapport exporté avec succès");
                } else {
                    Notification.notifError("Erreur", "Erreur lors de l'export du rapport");
                }
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'export : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }
}