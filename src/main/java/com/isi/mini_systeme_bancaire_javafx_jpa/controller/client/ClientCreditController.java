package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.RemboursementRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CreditServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CreditService;
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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientCreditController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private TabPane tabPane;

    // Onglet simulation
    @FXML
    private TextField txtMontantSimulation;

    @FXML
    private ComboBox<Integer> cbDureeSimulation;

    @FXML
    private TextField txtTauxSimulation;

    @FXML
    private Button btnSimuler;

    @FXML
    private VBox panelResultatSimulation;

    @FXML
    private Label lblMensualiteSimulation;

    @FXML
    private Label lblCoutTotalSimulation;

    @FXML
    private Label lblTEGSimulation;

    @FXML
    private TableView<SimulationMensualite> tableAmortissement;

    @FXML
    private TableColumn<SimulationMensualite, Integer> colPeriode;

    @FXML
    private TableColumn<SimulationMensualite, Double> colCapital;

    @FXML
    private TableColumn<SimulationMensualite, Double> colInteret;

    @FXML
    private TableColumn<SimulationMensualite, Double> colMensualite;

    @FXML
    private TableColumn<SimulationMensualite, Double> colRestant;

    // Onglet demande crédit
    @FXML
    private TextField txtMontantDemande;

    @FXML
    private ComboBox<Integer> cbDureeDemande;

    @FXML
    private TextField txtTauxDemande;

    @FXML
    private TextField txtMensualiteDemande;

    @FXML
    private TextArea txtMotifDemande;

    @FXML
    private Button btnEnvoyerDemande;

    // Onglet mes crédits
    @FXML
    private TableView<Credit> tableCredits;

    @FXML
    private TableColumn<Credit, Long> colIdCredit;

    @FXML
    private TableColumn<Credit, Double> colMontantCredit;

    @FXML
    private TableColumn<Credit, Double> colTauxCredit;

    @FXML
    private TableColumn<Credit, Integer> colDureeCredit;

    @FXML
    private TableColumn<Credit, Double> colMensualiteCredit;

    @FXML
    private TableColumn<Credit, String> colStatutCredit;

    @FXML
    private TableColumn<Credit, LocalDate> colDateCredit;

    @FXML
    private VBox panelDetailCredit;

    @FXML
    private Label lblDetailMontant;

    @FXML
    private Label lblDetailTaux;

    @FXML
    private Label lblDetailDuree;

    @FXML
    private Label lblDetailMensualite;

    @FXML
    private Label lblDetailDate;

    @FXML
    private Label lblDetailStatut;

    @FXML
    private Label lblDetailRembourse;

    @FXML
    private Label lblDetailRestant;

    @FXML
    private TableView<Remboursement> tableRemboursements;

    @FXML
    private TableColumn<Remboursement, LocalDate> colDateRemboursement;

    @FXML
    private TableColumn<Remboursement, Double> colMontantRemboursement;

    private CreditRepository creditRepository = new CreditRepository();
    private RemboursementRepository remboursementRepository = new RemboursementRepository();
    private CreditService creditService = new CreditServiceImpl();

    private ObservableList<Credit> creditsList = FXCollections.observableArrayList();
    private ObservableList<Remboursement> remboursementsList = FXCollections.observableArrayList();
    private ObservableList<SimulationMensualite> amortissementList = FXCollections.observableArrayList();

    // Classe pour le tableau d'amortissement
    public static class SimulationMensualite {
        private final Integer periode;
        private final Double capital;
        private final Double interet;
        private final Double mensualite;
        private final Double restant;

        public SimulationMensualite(Integer periode, Double capital, Double interet, Double mensualite, Double restant) {
            this.periode = periode;
            this.capital = capital;
            this.interet = interet;
            this.mensualite = mensualite;
            this.restant = restant;
        }

        public Integer getPeriode() { return periode; }
        public Double getCapital() { return capital; }
        public Double getInteret() { return interet; }
        public Double getMensualite() { return mensualite; }
        public Double getRestant() { return restant; }
    }

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

        // Initialiser les durées disponibles
        ObservableList<Integer> durees = FXCollections.observableArrayList(
                6, 12, 24, 36, 48, 60, 72, 84, 96, 120, 180, 240, 300, 360
        );
        cbDureeSimulation.setItems(durees);
        cbDureeSimulation.getSelectionModel().select(2); // 24 mois par défaut

        cbDureeDemande.setItems(durees);
        cbDureeDemande.getSelectionModel().select(2); // 24 mois par défaut

        // Configurer les colonnes du tableau d'amortissement
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capital"));
        colInteret.setCellValueFactory(new PropertyValueFactory<>("interet"));
        colMensualite.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colRestant.setCellValueFactory(new PropertyValueFactory<>("restant"));

        // Configurer les colonnes du tableau des crédits
        colIdCredit.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantCredit.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colTauxCredit.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colDureeCredit.setCellValueFactory(new PropertyValueFactory<>("dureeEnMois"));
        colMensualiteCredit.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colStatutCredit.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCredit.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));

        // Configurer les colonnes du tableau des remboursements
        colDateRemboursement.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMontantRemboursement.setCellValueFactory(new PropertyValueFactory<>("montant"));

        // Masquer le panel de résultat de simulation par défaut
        panelResultatSimulation.setVisible(false);

        // Masquer le panel de détail crédit par défaut
        panelDetailCredit.setVisible(false);

        // Ajouter un listener pour calculer la mensualité de demande de crédit
        txtMontantDemande.textProperty().addListener((observable, oldValue, newValue) -> {
            calculerMensualiteDemande();
        });

        txtTauxDemande.textProperty().addListener((observable, oldValue, newValue) -> {
            calculerMensualiteDemande();
        });

        cbDureeDemande.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculerMensualiteDemande();
        });

        // Charger les crédits du client
        chargerCredits();
    }

    private void chargerCredits() {
        try {
            // Récupérer tous les crédits du client connecté
            List<Credit> credits = creditRepository.findByClientId(SessionManager.getCurrentClient().getId());

            // Mettre à jour la liste observable
            creditsList.clear();
            creditsList.addAll(credits);

            // Mettre à jour le tableau
            tableCredits.setItems(creditsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des crédits : " + e.getMessage());
        }
    }

    private void calculerMensualiteDemande() {
        try {
            // Vérifier que tous les champs sont remplis
            if (txtMontantDemande.getText().isEmpty() || txtTauxDemande.getText().isEmpty() || cbDureeDemande.getValue() == null) {
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontantDemande.getText());
            double taux = Double.parseDouble(txtTauxDemande.getText());
            int duree = cbDureeDemande.getValue();

            // Calculer la mensualité
            double mensualite = creditService.calculerMensualite(montant, taux, duree);

            // Afficher la mensualité
            txtMensualiteDemande.setText(String.format("%.2f", mensualite));
        } catch (NumberFormatException e) {
            // Ignorer les erreurs de conversion (pendant la saisie)
        }
    }

    @FXML
    private void handleSimuler(ActionEvent event) {
        try {
            // Vérifier que tous les champs sont remplis
            if (txtMontantSimulation.getText().isEmpty() || txtTauxSimulation.getText().isEmpty() || cbDureeSimulation.getValue() == null) {
                Notification.notifWarning("Simulation", "Veuillez remplir tous les champs");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontantSimulation.getText());
            double taux = Double.parseDouble(txtTauxSimulation.getText());
            int duree = cbDureeSimulation.getValue();

            // Vérifier que les valeurs sont valides
            if (montant <= 0 || taux <= 0) {
                Notification.notifWarning("Simulation", "Les valeurs doivent être supérieures à zéro");
                return;
            }

            // Calculer la mensualité
            double mensualite = creditService.calculerMensualite(montant, taux, duree);

            // Calculer le montant total
            double montantTotal = creditService.calculerMontantTotal(mensualite, duree);

            // Calculer le coût total
            double coutTotal = montantTotal - montant;

            // Calculer le TEG
            double teg = creditService.calculerTauxEffectifGlobal(montant, mensualite, duree);

            // Afficher les résultats
            lblMensualiteSimulation.setText(String.format("%.2f FCFA", mensualite));
            lblCoutTotalSimulation.setText(String.format("%.2f FCFA", coutTotal));
            lblTEGSimulation.setText(String.format("%.2f %%", teg));

            // Générer le tableau d'amortissement
            genererTableauAmortissement(montant, taux, duree, mensualite);

            // Afficher le panel de résultat
            panelResultatSimulation.setVisible(true);
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        }
    }

    private void genererTableauAmortissement(double montant, double taux, int duree, double mensualite) {
        // Effacer la liste précédente
        amortissementList.clear();

        double tauxMensuel = taux / 12 / 100;
        double capitalRestant = montant;

        for (int i = 1; i <= duree; i++) {
            double interet = capitalRestant * tauxMensuel;
            double capitalRembourse = mensualite - interet;

            // Ajuster le dernier paiement si nécessaire pour éviter les erreurs d'arrondi
            if (i == duree) {
                capitalRembourse = capitalRestant;
                mensualite = capitalRembourse + interet;
            }

            capitalRestant -= capitalRembourse;

            // Ajouter à la liste
            amortissementList.add(new SimulationMensualite(
                    i,
                    Math.round(capitalRembourse * 100) / 100.0,
                    Math.round(interet * 100) / 100.0,
                    Math.round(mensualite * 100) / 100.0,
                    Math.round(capitalRestant * 100) / 100.0
            ));
        }

        // Mettre à jour le tableau
        tableAmortissement.setItems(amortissementList);
    }

    @FXML
    private void handleEnvoyerDemande(ActionEvent event) {
        try {
            // Vérifier que tous les champs sont remplis
            if (txtMontantDemande.getText().isEmpty() || txtTauxDemande.getText().isEmpty() || cbDureeDemande.getValue() == null) {
                Notification.notifWarning("Demande de crédit", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontantDemande.getText());
            double taux = Double.parseDouble(txtTauxDemande.getText());
            int duree = cbDureeDemande.getValue();
            double mensualite = Double.parseDouble(txtMensualiteDemande.getText());
            String motif = txtMotifDemande.getText();

            // Vérifier que les valeurs sont valides
            if (montant <= 0 || taux <= 0) {
                Notification.notifWarning("Demande de crédit", "Les valeurs doivent être supérieures à zéro");
                return;
            }

            // Demander confirmation
            boolean confirm = Notification.showConfirmationDialog(
                    "Confirmation",
                    "Demande de crédit",
                    "Êtes-vous sûr de vouloir faire cette demande de crédit?\n" +
                            "Montant: " + montant + " FCFA\n" +
                            "Taux: " + taux + "%\n" +
                            "Durée: " + duree + " mois\n" +
                            "Mensualité: " + mensualite + " FCFA"
            );

            if (confirm) {
                // Créer un nouveau crédit
                Credit credit = new Credit();
                credit.setMontant(montant);
                credit.setTauxInteret(taux);
                credit.setDureeEnMois(duree);
                credit.setMensualite(mensualite);
                credit.setDateDemande(LocalDate.now());
                credit.setStatut("En attente");
                credit.setClient(SessionManager.getCurrentClient());
                // Le motif pourrait être enregistré dans un champ de description si disponible

                // Sauvegarder le crédit
                credit = creditRepository.save(credit);

                if (credit != null) {
                    Notification.notifSuccess("Demande de crédit", "Votre demande de crédit a été envoyée avec succès. Elle sera étudiée par nos services dans les plus brefs délais.");

                    // Réinitialiser les champs
                    txtMontantDemande.clear();
                    txtTauxDemande.clear();
                    txtMensualiteDemande.clear();
                    txtMotifDemande.clear();

                    // Mettre à jour la liste des crédits
                    chargerCredits();

                    // Changer l'onglet pour afficher la liste des crédits
                    tabPane.getSelectionModel().select(2); // Onglet "Mes Crédits"
                }
            }
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la demande de crédit : " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectionCredit() {
        // Récupérer le crédit sélectionné
        Credit credit = tableCredits.getSelectionModel().getSelectedItem();

        if (credit != null) {
            // Afficher les détails du crédit
            lblDetailMontant.setText(String.format("%.2f FCFA", credit.getMontant()));
            lblDetailTaux.setText(String.format("%.2f%%", credit.getTauxInteret()));
            lblDetailDuree.setText(credit.getDureeEnMois() + " mois");
            lblDetailMensualite.setText(String.format("%.2f FCFA", credit.getMensualite()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblDetailDate.setText(credit.getDateDemande().format(formatter));

            lblDetailStatut.setText(credit.getStatut());

            // Charger les remboursements du crédit
            chargerRemboursements(credit);

            // Calculer le montant total remboursé
            double montantRembourse = credit.getRemboursements().stream()
                    .mapToDouble(Remboursement::getMontant)
                    .sum();

            // Calculer le montant total à rembourser
            double montantTotal = creditService.calculerMontantTotal(credit.getMensualite(), credit.getDureeEnMois());

            // Calculer le montant restant à rembourser
            double montantRestant = montantTotal - montantRembourse;

            // Afficher les montants
            lblDetailRembourse.setText(String.format("%.2f FCFA", montantRembourse));
            lblDetailRestant.setText(String.format("%.2f FCFA", montantRestant));

            // Afficher le panel de détail
            panelDetailCredit.setVisible(true);
        }
    }

    private void chargerRemboursements(Credit credit) {
        try {
            // Récupérer tous les remboursements du crédit
            List<Remboursement> remboursements = remboursementRepository.findByCreditId(credit.getId());

            // Mettre à jour la liste observable
            remboursementsList.clear();
            remboursementsList.addAll(remboursements);

            // Mettre à jour le tableau
            tableRemboursements.setItems(remboursementsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des remboursements : " + e.getMessage());
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