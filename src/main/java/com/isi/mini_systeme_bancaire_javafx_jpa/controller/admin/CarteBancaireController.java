package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.CarteBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
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
import org.hibernate.Hibernate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CarteBancaireController implements Initializable {

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtCVV;

    @FXML
    private DatePicker dpDateExpiration;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private PasswordField txtCodePin;

    @FXML
    private ComboBox<CompteWrapper> cbCompte;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnBloquer;

    @FXML
    private Button btnDebloquer;

    @FXML
    private TableView<CarteBancaire> tableCartes;

    @FXML
    private TableColumn<CarteBancaire, String> colNumero;

    @FXML
    private TableColumn<CarteBancaire, String> colClient;

    @FXML
    private TableColumn<CarteBancaire, String> colCompte;

    @FXML
    private TableColumn<CarteBancaire, LocalDate> colDateExpiration;

    @FXML
    private TableColumn<CarteBancaire, String> colStatut;

    private CarteBancaireRepository carteRepository = new CarteBancaireRepository();
    private CompteRepository compteRepository = new CompteRepository();
    private ObservableList<CarteBancaire> cartesList = FXCollections.observableArrayList();
    private ObservableList<CompteWrapper> comptesList = FXCollections.observableArrayList();
    private CarteBancaire selectedCarte;

    // Classe wrapper pour contourner les problèmes de lazy loading
    public static class CompteWrapper {
        private Compte compte;
        private String displayText;

        public CompteWrapper(Compte compte) {
            this.compte = compte;

            // Initialiser explicitement les propriétés du client
            if (compte.getClient() != null) {
                Hibernate.initialize(compte.getClient());
                this.displayText = compte.getNumero() + " (" +
                        compte.getClient().getNom() + " " +
                        compte.getClient().getPrenom() + ")";
            } else {
                this.displayText = compte.getNumero() + " (N/A)";
            }
        }

        public Compte getCompte() {
            return compte;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
           // Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les statuts disponibles
        cbStatut.setItems(FXCollections.observableArrayList("ACTIVE", "BLOQUEE", "EXPIREE"));

        // Configurer les colonnes du tableau
        configurerColonnesTableau();

        // Désactiver les boutons de modification et suppression au départ
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnBloquer.setDisable(true);
        btnDebloquer.setDisable(true);

        // Charger les comptes
        chargerComptes();

        // Charger les cartes
        chargerCartes();
    }

    private void configurerColonnesTableau() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));

        // Configuration de la colonne Client
        colClient.setCellValueFactory(cellData -> {
            CarteBancaire carte = cellData.getValue();
            if (carte.getCompte() != null && carte.getCompte().getClient() != null) {
                try {
                    // Forcer l'initialisation du client
                    Hibernate.initialize(carte.getCompte().getClient());
                    return javafx.beans.binding.Bindings.createStringBinding(() ->
                            carte.getCompte().getClient().getNom() + " " +
                                    carte.getCompte().getClient().getPrenom()
                    );
                } catch (Exception e) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
                }
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        // Configuration de la colonne Compte
        colCompte.setCellValueFactory(cellData -> {
            CarteBancaire carte = cellData.getValue();
            if (carte.getCompte() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(() ->
                        carte.getCompte().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colDateExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes
            List<Compte> comptes = compteRepository.findAll();

            // Convertir en CompteWrapper
            List<CompteWrapper> comptesWrapper = comptes.stream()
                    .map(CompteWrapper::new)
                    .collect(Collectors.toList());

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptesWrapper);

            // Mettre à jour le combobox
            cbCompte.setItems(comptesList);
        } catch (Exception e) {
          //  Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void chargerCartes() {
        try {
            // Récupérer toutes les cartes
            List<CarteBancaire> cartes = carteRepository.findAll();

            // Mettre à jour la liste observable
            cartesList.clear();
            cartesList.addAll(cartes);

            // Mettre à jour le tableau
            tableCartes.setItems(cartesList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des cartes : " + e.getMessage());
        }
    }

    @FXML
    private void handleCarteSelection() {
        // Récupérer la carte sélectionnée
        selectedCarte = tableCartes.getSelectionModel().getSelectedItem();

        if (selectedCarte != null) {
            // Remplir les champs avec les informations de la carte
            txtNumero.setText(selectedCarte.getNumero());
            txtCVV.setText(selectedCarte.getCvv());
            dpDateExpiration.setValue(selectedCarte.getDateExpiration());
            cbStatut.setValue(selectedCarte.getStatut());
            txtCodePin.setText(selectedCarte.getCodePin());

            // Trouver le compte correspondant dans la liste des comptes
            if (selectedCarte.getCompte() != null) {
                CompteWrapper compteCorrespondant = comptesList.stream()
                        .filter(cw -> cw.getCompte().getId().equals(selectedCarte.getCompte().getId()))
                        .findFirst()
                        .orElse(null);

                cbCompte.setValue(compteCorrespondant);
            } else {
                cbCompte.setValue(null);
            }

            // Activer les boutons de modification et suppression
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);

            // Activer/désactiver les boutons bloquer/débloquer selon le statut
            btnBloquer.setDisable("BLOQUEE".equals(selectedCarte.getStatut()) || "EXPIREE".equals(selectedCarte.getStatut()));
            btnDebloquer.setDisable(!"BLOQUEE".equals(selectedCarte.getStatut()));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Vérifier que tous les champs obligatoires sont remplis
            if (txtNumero.getText().isEmpty() || txtCVV.getText().isEmpty() || dpDateExpiration.getValue() == null ||
                    cbStatut.getValue() == null || txtCodePin.getText().isEmpty() || cbCompte.getValue() == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Créer une carte
            CarteBancaire carte = new CarteBancaire();
            carte.setNumero(txtNumero.getText());
            carte.setCvv(txtCVV.getText());
            carte.setDateExpiration(dpDateExpiration.getValue());
            carte.setStatut(cbStatut.getValue());
            carte.setCodePin(txtCodePin.getText());
            carte.setCompte(cbCompte.getValue().getCompte());  // Utiliser le getter du wrapper

            // Enregistrer la carte
            carte = carteRepository.save(carte);

            if (carte != null) {
                Notification.notifSuccess("Carte bancaire", "Carte créée avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les cartes
                chargerCartes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la création de la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Vérifier qu'une carte est sélectionnée
            if (selectedCarte == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
                return;
            }

            // Vérifier que tous les champs obligatoires sont remplis
            if (txtNumero.getText().isEmpty() || txtCVV.getText().isEmpty() || dpDateExpiration.getValue() == null ||
                    cbStatut.getValue() == null || txtCodePin.getText().isEmpty() || cbCompte.getValue() == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Mettre à jour la carte
            selectedCarte.setNumero(txtNumero.getText());
            selectedCarte.setCvv(txtCVV.getText());
            selectedCarte.setDateExpiration(dpDateExpiration.getValue());
            selectedCarte.setStatut(cbStatut.getValue());
            selectedCarte.setCodePin(txtCodePin.getText());
            selectedCarte.setCompte(cbCompte.getValue().getCompte());  // Utiliser le getter du wrapper

            // Enregistrer la carte
            CarteBancaire carte = carteRepository.save(selectedCarte);

            if (carte != null) {
                Notification.notifSuccess("Carte bancaire", "Carte mise à jour avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les cartes
                chargerCartes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour de la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Vérifier qu'une carte est sélectionnée
            if (selectedCarte == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
                return;
            }

            // Demander confirmation
            boolean confirm = Notification.confirmDelete();

            if (confirm) {
                // Supprimer la carte
                carteRepository.deleteById(selectedCarte.getId());

                Notification.notifSuccess("Carte bancaire", "Carte supprimée avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les cartes
                chargerCartes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la suppression de la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        txtNumero.clear();
        txtCVV.clear();
        dpDateExpiration.setValue(null);
        cbStatut.setValue(null);
        txtCodePin.clear();
        cbCompte.setValue(null);

        selectedCarte = null;
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnBloquer.setDisable(true);
        btnDebloquer.setDisable(true);
        btnSave.setDisable(false);

        // Désélectionner la ligne du tableau
        tableCartes.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBloquer(ActionEvent event) {
        try {
            // Vérifier qu'une carte est sélectionnée
            if (selectedCarte == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
                return;
            }

            // Bloquer la carte
            selectedCarte.setStatut("BLOQUEE");

            // Enregistrer la carte
            CarteBancaire carte = carteRepository.save(selectedCarte);

            if (carte != null) {
                Notification.notifSuccess("Carte bancaire", "Carte bloquée avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les cartes
                chargerCartes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du blocage de la carte : " + e.getMessage());
        }
    }
    @FXML
    private void handleDebloquer(ActionEvent event) {
        try {
            // Vérifier qu'une carte est sélectionnée
            if (selectedCarte == null) {
                Notification.notifWarning("Carte bancaire", "Veuillez sélectionner une carte");
                return;
            }

            // Débloquer la carte
            selectedCarte.setStatut("ACTIVE");

            // Enregistrer la carte
            CarteBancaire carte = carteRepository.save(selectedCarte);

            if (carte != null) {
                Notification.notifSuccess("Carte bancaire", "Carte débloquée avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les cartes
                chargerCartes();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du déblocage de la carte : " + e.getMessage());
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