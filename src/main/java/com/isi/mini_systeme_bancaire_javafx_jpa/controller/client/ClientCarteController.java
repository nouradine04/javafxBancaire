package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.*;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CarteBancaireRepository;
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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientCarteController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private ComboBox<Compte> cbComptes;

    @FXML
    private TableView<CarteBancaire> tableCartes;

    @FXML
    private TableColumn<CarteBancaire, String> colNumero;

    @FXML
    private TableColumn<CarteBancaire, LocalDate> colDateExpiration;

    @FXML
    private TableColumn<CarteBancaire, String> colStatut;

    @FXML
    private Button btnDemander;

    @FXML
    private VBox panelInfoCarte;

    @FXML
    private Label lblNumero;

    @FXML
    private Label lblDateExpiration;

    @FXML
    private Label lblStatut;

    @FXML
    private Button btnBloquer;

    @FXML
    private Button btnDebloquer;

    private CompteRepository compteRepository = new CompteRepository();
    private CarteBancaireRepository carteRepository = new CarteBancaireRepository();

    private ObservableList<Compte> comptesList = FXCollections.observableArrayList();
    private ObservableList<CarteBancaire> cartesList = FXCollections.observableArrayList();

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
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colDateExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Cacher le panel d'information de carte par défaut
        panelInfoCarte.setVisible(false);

        // Ajouter un listener pour le changement de compte
        cbComptes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerCartes(newValue);
            }
        });

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

            // Mettre à jour le combobox
            cbComptes.setItems(comptesList);

            // Sélectionner le premier compte par défaut
            if (!comptes.isEmpty()) {
                cbComptes.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void chargerCartes(Compte compte) {
        try {
            // Récupérer toutes les cartes du compte
            List<CarteBancaire> cartes = carteRepository.findByCompteId(compte.getId());

            // Mettre à jour la liste observable
            cartesList.clear();
            cartesList.addAll(cartes);

            // Mettre à jour le tableau
            tableCartes.setItems(cartesList);

            // Cacher le panel d'information de carte
            panelInfoCarte.setVisible(false);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des cartes : " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectionCarte() {
        // Récupérer la carte sélectionnée
        CarteBancaire carte = tableCartes.getSelectionModel().getSelectedItem();

        if (carte != null) {
            // Afficher les informations de la carte
            lblNumero.setText(formatNumeroCarteCache(carte.getNumero()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            lblDateExpiration.setText(carte.getDateExpiration().format(formatter));

            lblStatut.setText(carte.getStatut());

            // Afficher le panel d'information de carte
            panelInfoCarte.setVisible(true);

            // Activer/désactiver les boutons selon le statut de la carte
            btnBloquer.setDisable("BLOQUEE".equals(carte.getStatut()) || "EXPIREE".equals(carte.getStatut()));
            btnDebloquer.setDisable(!"BLOQUEE".equals(carte.getStatut()));
        }
    }

    private String formatNumeroCarteCache(String numero) {
        if (numero == null || numero.length() < 16) {
            return "XXXX-XXXX-XXXX-XXXX";
        }
        return "XXXX-XXXX-XXXX-" + numero.substring(12);
    }

    @FXML
    private void handleDemander(ActionEvent event) {
        // Vérifier qu'un compte est sélectionné
        if (cbComptes.getSelectionModel().isEmpty()) {
            Notification.notifWarning("Carte bancaire", "Veuillez sélectionner un compte");
            return;
        }

        Compte compte = cbComptes.getSelectionModel().getSelectedItem();

        // Vérifier si le compte a déjà une carte active
        boolean carteActive = cartesList.stream()
                .anyMatch(c -> "ACTIVE".equals(c.getStatut()));

        if (carteActive) {
            Notification.notifWarning("Carte bancaire", "Ce compte possède déjà une carte active");
            return;
        }

        // Demander confirmation
        boolean confirm = Notification.showConfirmationDialog(
                "Confirmation",
                "Demande de carte bancaire",
                "Êtes-vous sûr de vouloir demander une carte bancaire pour ce compte?"
        );

        if (confirm) {
            try {
                // Créer une nouvelle carte
                CarteBancaire carte = new CarteBancaire();
                carte.setNumero(Outils.generateCardNumber());
                carte.setCvv(Outils.generateCVV());
                carte.setDateExpiration(LocalDate.now().plusYears(4));
                carte.setStatut("EN_ATTENTE");
                carte.setCompte(compte);

                // Sauvegarder la carte
                carte = carteRepository.save(carte);

                if (carte != null) {
                    Notification.notifSuccess("Carte bancaire", "Demande de carte effectuée avec succès. Vous recevrez votre carte par courrier sous 5 à 7 jours ouvrés.");

                    // Mettre à jour la liste des cartes
                    chargerCartes(compte);
                }
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors de la demande de carte : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBloquer(ActionEvent event) {
        // Récupérer la carte sélectionnée
        CarteBancaire carte = tableCartes.getSelectionModel().getSelectedItem();

        if (carte == null) {
            Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
            return;
        }

        // Demander confirmation
        boolean confirm = Notification.showConfirmationDialog(
                "Confirmation",
                "Blocage de carte",
                "Êtes-vous sûr de vouloir bloquer cette carte bancaire?"
        );

        if (confirm) {
            try {
                // Bloquer la carte
                carte.setStatut("BLOQUEE");

                // Sauvegarder la carte
                carte = carteRepository.save(carte);

                if (carte != null) {
                    Notification.notifSuccess("Carte bancaire", "Carte bloquée avec succès");

                    // Mettre à jour l'affichage
                    lblStatut.setText(carte.getStatut());
                    btnBloquer.setDisable(true);
                    btnDebloquer.setDisable(false);

                    // Mettre à jour la liste des cartes
                    chargerCartes(cbComptes.getSelectionModel().getSelectedItem());
                }
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors du blocage de la carte : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDebloquer(ActionEvent event) {
        // Récupérer la carte sélectionnée
        CarteBancaire carte = tableCartes.getSelectionModel().getSelectedItem();

        if (carte == null) {
            Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
            return;
        }

        // Demander confirmation
        boolean confirm = Notification.showConfirmationDialog(
                "Confirmation",
                "Déblocage de carte",
                "Êtes-vous sûr de vouloir débloquer cette carte bancaire?"
        );

        if (confirm) {
            try {
                // Débloquer la carte
                carte.setStatut("ACTIVE");

                // Sauvegarder la carte
                carte = carteRepository.save(carte);

                if (carte != null) {
                    Notification.notifSuccess("Carte bancaire", "Carte débloquée avec succès");

                    // Mettre à jour l'affichage
                    lblStatut.setText(carte.getStatut());
                    btnBloquer.setDisable(false);
                    btnDebloquer.setDisable(true);

                    // Mettre à jour la liste des cartes
                    chargerCartes(cbComptes.getSelectionModel().getSelectedItem());
                }
            } catch (Exception e) {
                Notification.notifError("Erreur", "Erreur lors du déblocage de la carte : " + e.getMessage());
            }
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