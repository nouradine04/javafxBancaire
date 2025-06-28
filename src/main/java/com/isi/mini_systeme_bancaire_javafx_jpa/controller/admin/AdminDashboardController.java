package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.TicketSupportService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.TicketSupportServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML
    private Label lblTotalClients;

    @FXML
    private Label lblTotalComptes;

    @FXML
    private Label lblTotalTransactions;

    @FXML
    private Label lblTotalSolde;

    @FXML
    private Label lblTotalCredits;

    @FXML
    private Label lblTicketsOuverts;

    @FXML
    private LineChart<String, Number> chartTransactions;

    @FXML
    private PieChart chartComptes;

    @FXML
    private TableView<Transaction> tableRecentTransactions;

    @FXML
    private TableColumn<Transaction, String> colType;

    @FXML
    private TableColumn<Transaction, Double> colMontant;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;

    @FXML
    private TableColumn<Transaction, String> colStatut;

    private ClientRepository clientRepository = new ClientRepository();
    private CompteRepository compteRepository = new CompteRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();
    private CreditRepository creditRepository = new CreditRepository();
    private TicketSupportService ticketService = new TicketSupportServiceImpl();

    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Configurer les colonnes du tableau
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Charger les données
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            // Compter le nombre de clients
            List<Client> clients = clientRepository.findAll();
            long nombreClients = clients.size();
            lblTotalClients.setText(String.valueOf(nombreClients));

            // Compter le nombre de comptes
            List<Compte> comptes = compteRepository.findAll();
            long nombreComptes = comptes.size();
            lblTotalComptes.setText(String.valueOf(nombreComptes));

            // Compter le nombre de transactions
            List<Transaction> transactions = transactionRepository.findAll();
            long nombreTransactions = transactions.size();
            lblTotalTransactions.setText(String.valueOf(nombreTransactions));

            // Calculer le solde total
            double soldeTotal = comptes.stream()
                    .mapToDouble(Compte::getSolde)
                    .sum();
            lblTotalSolde.setText(String.format("%.2f FCFA", soldeTotal));

            // Compter le nombre de crédits
            long nombreCredits = creditRepository.findAll().size();
            lblTotalCredits.setText(String.valueOf(nombreCredits));

            // Compter le nombre de tickets ouverts
            int ticketsOuverts = ticketService.getNombreTicketsOuverts();
            lblTicketsOuverts.setText(String.valueOf(ticketsOuverts));

            // Charger les transactions récentes
            chargerTransactionsRecentes();

            // Mettre à jour les graphiques
            mettreAJourGraphiqueTransactions();
            mettreAJourGraphiqueComptes();
        } catch (Exception e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void chargerTransactionsRecentes() {
        try {
            // Récupérer les transactions récentes (limité à 10)
            List<Transaction> transactions = transactionRepository.findAll().stream()
                    .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                    .limit(10)
                    .collect(Collectors.toList());

            // Mettre à jour la liste observable
            transactionsList.clear();
            transactionsList.addAll(transactions);

            // Mettre à jour le tableau
            tableRecentTransactions.setItems(transactionsList);
        } catch (Exception e) {
           // Notification.notifError("Erreur", "Erreur lors du chargement des transactions récentes : " + e.getMessage());
        }
    }

    private void mettreAJourGraphiqueTransactions() {
        try {
            // Effacer les données précédentes
            chartTransactions.getData().clear();

            // Créer une série pour les transactions
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de transactions");

            // Récupérer toutes les transactions
            List<Transaction> allTransactions = transactionRepository.findAll();

            // Filtrer pour obtenir les transactions des 7 derniers jours
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Transaction> transactions = allTransactions.stream()
                    .filter(t -> t.getDate().isAfter(sevenDaysAgo))
                    .collect(Collectors.toList());

            // Grouper par jour
            Map<String, Long> transactionsParJour = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            // Initialiser les 7 derniers jours avec zéro
            for (int i = 0; i < 7; i++) {
                String jour = LocalDate.now().minusDays(i).format(formatter);
                transactionsParJour.put(jour, 0L);
            }

            // Compter les transactions par jour
            for (Transaction transaction : transactions) {
                String jour = transaction.getDate().toLocalDate().format(formatter);
                transactionsParJour.put(jour, transactionsParJour.getOrDefault(jour, 0L) + 1);
            }

            // Ajouter les données au graphique (dans l'ordre des dates)
            for (int i = 6; i >= 0; i--) {
                String jour = LocalDate.now().minusDays(i).format(formatter);
                series.getData().add(new XYChart.Data<>(jour, transactionsParJour.getOrDefault(jour, 0L)));
            }

            // Ajouter la série au graphique
            chartTransactions.getData().add(series);
        } catch (Exception e) {
           // Notification.notifError("Erreur", "Erreur lors de la mise à jour du graphique des transactions : " + e.getMessage());
        }
    }

    private void mettreAJourGraphiqueComptes() {
        try {
            // Effacer les données précédentes
            chartComptes.getData().clear();

            // Récupérer tous les comptes
            List<Compte> comptes = compteRepository.findAll();

            // Compter les comptes par type
            Map<String, Long> comptesParType = comptes.stream()
                    .collect(Collectors.groupingBy(Compte::getType, Collectors.counting()));

            // Créer les données du graphique
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Long> entry : comptesParType.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }

            // Mettre à jour le graphique
            chartComptes.setData(pieChartData);
        } catch (Exception e) {
           // Notification.notifError("Erreur", "Erreur lors de la mise à jour du graphique des comptes : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionClients(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des clients", "/fxml/admin/client.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des clients : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionComptes(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des comptes", "/fxml/admin/compte.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des comptes : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionTransactions(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des transactions", "/fxml/admin/transaction.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des transactions : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionCartes(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des cartes bancaires", "/fxml/admin/carte.fxml");
        } catch (IOException e) {
           // Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des cartes : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionCredits(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des crédits", "/fxml/admin/credit.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des crédits : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionTickets(ActionEvent event) {
        try {
            Outils.load(event, "Support client", "/fxml/admin/ticket.fxml");
        } catch (IOException e) {
            //Notification.notifError("Erreur", "Erreur lors du chargement du support client : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnalyseOperations(ActionEvent event) {
        try {
            Outils.load(event, "Analyse des opérations", "/fxml/admin/operation.fxml");
        } catch (IOException e) {
           // Notification.notifError("Erreur", "Erreur lors du chargement de l'analyse des opérations : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        SessionManager.logout();

        try {
            Outils.load(event, "Connexion", "/fxml/login.fxml");
        } catch (IOException e) {
         }
    }
}