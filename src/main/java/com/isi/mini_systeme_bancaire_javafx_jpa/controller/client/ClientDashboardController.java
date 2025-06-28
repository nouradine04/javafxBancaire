package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private ComboBox<Compte> cbComptes;

    @FXML
    private Label lblSolde;

    @FXML
    private Label lblNumeroCompte;

    @FXML
    private Label lblTypeCompte;

    @FXML
    private Label lblDateCreation;

    @FXML
    private Label lblNombreTransactions;

    @FXML
    private LineChart<String, Number> chartSolde;

    @FXML
    private TableView<Transaction> tableTransactions;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;

    @FXML
    private TableColumn<Transaction, String> colType;

    @FXML
    private TableColumn<Transaction, Double> colMontant;

    @FXML
    private TableColumn<Transaction, String> colStatut;

    private CompteRepository compteRepository = new CompteRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    private ObservableList<Compte> comptesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Vérifier si un client est connecté
            if (!SessionManager.isClientLoggedIn()) {
                Notification.notifError("Erreur", "Aucun client connecté");
                return;
            }

            // Récupérer le client connecté
            Client client = SessionManager.getCurrentClient();
            if (client == null) {
                Notification.notifError("Erreur", "Client invalide");
                return;
            }

            // Afficher le nom du client
            lblNomClient.setText(client.getNom() + " " + client.getPrenom());

            // Configurer le combobox des comptes et les colonnes du tableau
            configurerUI();

            // Charger les comptes du client
            chargerComptes();
        } catch (Exception e) {
            System.err.println("Exception dans initialize: " + e.getMessage());
            e.printStackTrace();
            Notification.notifError("Erreur", "Erreur d'initialisation: " + e.getMessage());
        }
    }

    private void configurerUI() {
        // Configurer le combobox des comptes
        cbComptes.setConverter(new javafx.util.StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                return compte == null ? "" : compte.getNumero() + " (" + compte.getType() + ")";
            }

            @Override
            public Compte fromString(String s) {
                return null; // Non utilisé
            }
        });

        // Configurer les colonnes du tableau
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(column -> new TableCell<Transaction, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f FCFA", item));
                }
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Ajouter un listener pour le changement de compte
        cbComptes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                afficherDetailsCompte(newValue);
            }
        });
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes du client connecté
            List<Compte> comptes = compteRepository.findByClientId(SessionManager.getCurrentClient().getId());

            if (comptes.isEmpty()) {
                Notification.notifInfo("Information", "Vous n'avez aucun compte actif");
                return;
            }

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour le combobox
            cbComptes.setItems(comptesList);

            // Sélectionner le premier compte par défaut
            if (!comptes.isEmpty()) {
                cbComptes.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            System.err.println("Exception dans chargerComptes: " + e.getMessage());
            e.printStackTrace();
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void afficherDetailsCompte(Compte compte) {
        try {
            if (compte == null) {
                System.err.println("Compte null dans afficherDetailsCompte");
                return;
            }

            // Recharger le compte depuis la BD pour avoir des données fraîches
            Optional<Compte> compteRecharge = compteRepository.findById(compte.getId());
            if (!compteRecharge.isPresent()) {
                System.err.println("Compte non trouvé dans la BD: " + compte.getId());
                return;
            }

            compte = compteRecharge.get();

            // Afficher les détails du compte
            lblSolde.setText(String.format("%.2f FCFA", compte.getSolde()));
            lblNumeroCompte.setText(compte.getNumero() != null ? compte.getNumero() : "N/A");
            lblTypeCompte.setText(compte.getType() != null ? compte.getType() : "N/A");

            if (compte.getDateCreation() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                lblDateCreation.setText(compte.getDateCreation().format(formatter));
            } else {
                lblDateCreation.setText("N/A");
            }

            // Charger les transactions du compte
            chargerTransactions(compte);

            // Mettre à jour le graphique
            mettreAJourGraphique(compte);
        } catch (Exception e) {
            System.err.println("Exception dans afficherDetailsCompte: " + e.getMessage());
            e.printStackTrace();
            Notification.notifError("Erreur", "Erreur lors de l'affichage des détails du compte: " + e.getMessage());
        }
    }

    private void chargerTransactions(Compte compte) {
        try {
            if (compte == null || compte.getId() == null) {
                System.err.println("Compte invalide dans chargerTransactions");
                lblNombreTransactions.setText("0");
                tableTransactions.setItems(FXCollections.observableArrayList());
                return;
            }

            System.out.println("Chargement des transactions pour le compte " + compte.getNumero() + " (ID: " + compte.getId() + ")");

            // Essayer d'abord avec la requête JPQL
            List<Transaction> transactions = transactionRepository.findByCompteId(compte.getId());

            // Si pas de résultats, essayer avec la requête SQL native
            if (transactions.isEmpty()) {
                System.out.println("Aucune transaction trouvée avec JPQL, essai avec SQL native");
                transactions = transactionRepository.findByCompteIdNative(compte.getId());
            }

            // Mettre à jour le nombre de transactions
            lblNombreTransactions.setText(String.valueOf(transactions.size()));

            System.out.println("Nombre de transactions trouvées: " + transactions.size());

            // Lister les transactions pour le débogage
            for (Transaction t : transactions) {
                System.out.println("Transaction ID: " + t.getId() +
                        ", Type: " + t.getType() +
                        ", Montant: " + t.getMontant() +
                        ", Date: " + t.getDate());
            }

            // Créer une nouvelle liste observable avec les transactions
            ObservableList<Transaction> transactionsObs = FXCollections.observableArrayList(transactions);

            // Assigner la liste au tableau
            tableTransactions.setItems(transactionsObs);

            // Forcer le rafraîchissement du tableau
            tableTransactions.refresh();
        } catch (Exception e) {
            System.err.println("Exception dans chargerTransactions: " + e.getMessage());
            e.printStackTrace();
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions: " + e.getMessage());
        }
    }

    private void mettreAJourGraphique(Compte compte) {
        try {
            // Effacer les données précédentes
            chartSolde.getData().clear();

            // Créer une série pour l'évolution du solde
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Évolution du solde");

            // Récupérer les transactions du compte
            List<Transaction> transactions = transactionRepository.findByCompteId(compte.getId());

            // Si pas de résultats, essayer avec la requête SQL native
            if (transactions.isEmpty()) {
                transactions = transactionRepository.findByCompteIdNative(compte.getId());
            }

            // Si aucune transaction, afficher uniquement le solde actuel
            if (transactions.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                series.getData().add(new XYChart.Data<>(LocalDate.now().format(formatter), compte.getSolde()));
            } else {
                // Trier les transactions par date
                transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

                // Commencer avec le solde initial (estimation basée sur les transactions)
                double soldeInitial = compte.getSolde();
                for (Transaction t : transactions) {
                    if (t.getType().equals("DEPOT") && compte.getId().equals(t.getCompte() != null ? t.getCompte().getId() : null)) {
                        soldeInitial -= t.getMontant();
                    } else if (t.getType().equals("RETRAIT") && compte.getId().equals(t.getCompte() != null ? t.getCompte().getId() : null)) {
                        soldeInitial += t.getMontant();
                    } else if (t.getType().equals("VIREMENT")) {
                        if (compte.getId().equals(t.getCompteSource() != null ? t.getCompteSource().getId() : null)) {
                            soldeInitial += t.getMontant();
                        } else if (compte.getId().equals(t.getCompteDestination() != null ? t.getCompteDestination().getId() : null)) {
                            soldeInitial -= t.getMontant();
                        }
                    }
                }

                // Assurer que le solde initial est au moins 0
                soldeInitial = Math.max(0, soldeInitial);

                double solde = soldeInitial;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

                // Ajouter le point initial
                series.getData().add(new XYChart.Data<>(
                        transactions.get(0).getDate().minusDays(1).toLocalDate().format(formatter),
                        solde
                ));

                for (Transaction transaction : transactions) {
                    try {
                        if (transaction.getType().equals("DEPOT")) {
                            // Si c'est un dépôt sur ce compte
                            if (transaction.getCompte() != null && compte.getId().equals(transaction.getCompte().getId())) {
                                solde += transaction.getMontant();
                            }
                        } else if (transaction.getType().equals("RETRAIT")) {
                            // Si c'est un retrait de ce compte
                            if (transaction.getCompte() != null && compte.getId().equals(transaction.getCompte().getId())) {
                                solde -= transaction.getMontant();
                            }
                        } else if (transaction.getType().equals("VIREMENT")) {
                            // Si ce compte est la source du virement
                            if (transaction.getCompteSource() != null && compte.getId().equals(transaction.getCompteSource().getId())) {
                                solde -= transaction.getMontant();
                            }
                            // Si ce compte est la destination du virement
                            else if (transaction.getCompteDestination() != null && compte.getId().equals(transaction.getCompteDestination().getId())) {
                                solde += transaction.getMontant();
                            }
                        }

                        // Ajouter le point au graphique
                        series.getData().add(new XYChart.Data<>(
                                transaction.getDate().toLocalDate().format(formatter),
                                solde
                        ));
                    } catch (NullPointerException e) {
                        System.out.println("Erreur avec la transaction ID=" + transaction.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Ajouter la série au graphique
            chartSolde.getData().add(series);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du graphique : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        Notification.notifInfo("Export", "Fonctionnalité d'export PDF à implémenter");
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
    private void handleTicket(ActionEvent event) {
        try {
            Outils.load(event, "Support client", "/fxml/client/ticket.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du support client : " + e.getMessage());
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