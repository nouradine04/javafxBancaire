package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
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

public class CreditController implements Initializable {

    @FXML
    private TableView<Credit> tableCredits;

    @FXML
    private TableColumn<Credit, String> colClient;
    @FXML
    private TableColumn<Credit, Double> colMontant;
    @FXML
    private TableColumn<Credit, Double> colTaux;
    @FXML
    private TableColumn<Credit, Integer> colDuree;
    @FXML
    private TableColumn<Credit, Double> colMensualite;
    @FXML
    private TableColumn<Credit, LocalDate> colDateDemande;
    @FXML
    private TableColumn<Credit, String> colStatut;

    @FXML
    private ComboBox<String> cbFiltreStatut;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;

    private final CreditRepository creditRepository = new CreditRepository();
    private final ObservableList<Credit> creditsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les filtres de statut
        initialiserFiltresStatut();

        // Configurer les colonnes du tableau
        configurerColonnesTableau();

        // Charger les crédits
        chargerCredits();

        // Configurer les écouteurs pour les filtres
        configurerEcouteursFiltre();
    }

    private void initialiserFiltresStatut() {
        cbFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Approuvé", "Refusé", "En cours", "Remboursé"
        ));
        cbFiltreStatut.setValue("Tous");
    }

    private void configurerEcouteursFiltre() {
        cbFiltreStatut.setOnAction(event -> filtrerCredits());
        dpDateDebut.setOnAction(event -> filtrerCredits());
        dpDateFin.setOnAction(event -> filtrerCredits());
    }

    private void configurerColonnesTableau() {
        // Configuration des colonnes
        colClient.setCellValueFactory(cellData -> {
            Credit credit = cellData.getValue();
            String clientInfo = credit.getClient() != null
                    ? credit.getClient().getNom() + " " + credit.getClient().getPrenom()
                    : "N/A";
            return javafx.beans.binding.Bindings.createStringBinding(() -> clientInfo);
        });

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeEnMois"));
        colMensualite.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colDateDemande.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void chargerCredits() {
        try {
            // Récupérer tous les crédits
            List<Credit> credits = creditRepository.findAll();

            // Mettre à jour la liste
            creditsList.clear();
            creditsList.addAll(credits);

            // Mettre à jour le tableau
            tableCredits.setItems(creditsList);

        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des crédits : " + e.getMessage());
        }
    }

    private void filtrerCredits() {
        try {
            // Récupérer tous les crédits
            List<Credit> credits = creditRepository.findAll();

            // Filtrer par statut
            String statutSelectionne = cbFiltreStatut.getValue();
            if (!"Tous".equals(statutSelectionne)) {
                credits = credits.stream()
                        .filter(c -> c.getStatut().equals(statutSelectionne))
                        .collect(Collectors.toList());
            }

            // Filtrer par plage de dates
            LocalDate dateDebut = dpDateDebut.getValue();
            LocalDate dateFin = dpDateFin.getValue();

            if (dateDebut != null && dateFin != null) {
                credits = credits.stream()
                        .filter(c -> !c.getDateDemande().isBefore(dateDebut) &&
                                !c.getDateDemande().isAfter(dateFin))
                        .collect(Collectors.toList());
            }

            // Mettre à jour la liste
            creditsList.clear();
            creditsList.addAll(credits);

            // Mettre à jour le tableau
            tableCredits.setItems(creditsList);

        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du filtrage des crédits : " + e.getMessage());
        }
    }

    @FXML
    private void handleApprobationCredit() {
        Credit selectedCredit = tableCredits.getSelectionModel().getSelectedItem();

        if (selectedCredit == null) {
            Notification.notifWarning("Approbation", "Veuillez sélectionner un crédit à approuver");
            return;
        }

        // Vérifier le statut actuel
        if (!"En attente".equals(selectedCredit.getStatut())) {
            Notification.notifWarning("Approbation", "Seuls les crédits en attente peuvent être approuvés");
            return;
        }

        // Boîte de confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation d'approbation");
        confirmDialog.setHeaderText("Voulez-vous vraiment approuver ce crédit ?");
        confirmDialog.setContentText(
                "Client : " + selectedCredit.getClient().getNom() + " " + selectedCredit.getClient().getPrenom() +
                        "\nMontant : " + selectedCredit.getMontant() + " FCFA" +
                        "\nDurée : " + selectedCredit.getDureeEnMois() + " mois"
        );

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Mettre à jour le statut
                selectedCredit.setStatut("Approuvé");

                // Enregistrer la mise à jour
                creditRepository.save(selectedCredit);

                // Rafraîchir la liste
                chargerCredits();

                Notification.notifSuccess("Approbation", "Crédit approuvé avec succès");
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors de l'approbation : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRejeterCredit() {
        Credit selectedCredit = tableCredits.getSelectionModel().getSelectedItem();

        if (selectedCredit == null) {
            Notification.notifWarning("Rejet", "Veuillez sélectionner un crédit à rejeter");
            return;
        }

        // Vérifier le statut actuel
        if (!"En attente".equals(selectedCredit.getStatut())) {
            Notification.notifWarning("Rejet", "Seuls les crédits en attente peuvent être rejetés");
            return;
        }

        // Boîte de confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de rejet");
        confirmDialog.setHeaderText("Voulez-vous vraiment rejeter ce crédit ?");
        confirmDialog.setContentText(
                "Client : " + selectedCredit.getClient().getNom() + " " + selectedCredit.getClient().getPrenom() +
                        "\nMontant : " + selectedCredit.getMontant() + " FCFA" +
                        "\nDurée : " + selectedCredit.getDureeEnMois() + " mois"
        );

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Mettre à jour le statut
                selectedCredit.setStatut("Refusé");

                // Enregistrer la mise à jour
                creditRepository.save(selectedCredit);

                // Rafraîchir la liste
                chargerCredits();

                Notification.notifSuccess("Rejet", "Crédit rejeté avec succès");
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors du rejet : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDetailCredit() {
        Credit selectedCredit = tableCredits.getSelectionModel().getSelectedItem();

        if (selectedCredit == null) {
            Notification.notifWarning("Détails", "Veuillez sélectionner un crédit");
            return;
        }

        // Afficher les détails dans une boîte de dialogue
        Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
        detailsDialog.setTitle("Détails du Crédit");
        detailsDialog.setHeaderText("Informations détaillées du crédit");
        detailsDialog.setContentText(
                "Client : " + selectedCredit.getClient().getNom() + " " + selectedCredit.getClient().getPrenom() +
                        "\nEmail : " + selectedCredit.getClient().getEmail() +
                        "\n\nDétails du Crédit :" +
                        "\nMontant : " + selectedCredit.getMontant() + " FCFA" +
                        "\nTaux d'intérêt : " + selectedCredit.getTauxInteret() + " %" +
                        "\nDurée : " + selectedCredit.getDureeEnMois() + " mois" +
                        "\nMensualité : " + selectedCredit.getMensualite() + " FCFA" +
                        "\nDate de demande : " + selectedCredit.getDateDemande() +
                        "\nStatut : " + selectedCredit.getStatut()
        );

        detailsDialog.showAndWait();
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