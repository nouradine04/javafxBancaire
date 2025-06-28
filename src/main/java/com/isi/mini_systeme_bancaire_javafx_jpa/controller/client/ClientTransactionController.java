package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CompteServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientTransactionController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private ComboBox<Compte> cbCompteSource;

    @FXML
    private ComboBox<Compte> cbCompteDestination;

    @FXML
    private TextField txtMontant;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblSoldeSource;

    @FXML
    private Label lblSoldeDestination;

    @FXML
    private RadioButton rbDepot;

    @FXML
    private RadioButton rbRetrait;

    @FXML
    private RadioButton rbVirement;

    @FXML
    private VBox panelVirement;

    @FXML
    private Button btnValider;

    private CompteService compteService;
    private CompteRepository compteRepository;
    private ClientRepository clientRepository;
    private TransactionRepository transactionRepository;
    private Long clientId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les services et repositories
        compteService = new CompteServiceImpl();
        compteRepository = new CompteRepository();
        clientRepository = new ClientRepository();
        transactionRepository = new TransactionRepository();

        // Vérifier si un client est connecté
        if (SessionManager.isClientLoggedIn()) {
            clientId = SessionManager.getCurrentClient().getId();
            // Charger les informations du client
            loadClientInfo();
        } else {
            Notification.notifError("Erreur", "Aucun client connecté");
            return;
        }

        // Groupe de boutons radio pour le type de transaction
        ToggleGroup toggleGroup = new ToggleGroup();
        rbDepot.setToggleGroup(toggleGroup);
        rbRetrait.setToggleGroup(toggleGroup);
        rbVirement.setToggleGroup(toggleGroup);

        // Par défaut, on choisit le dépôt
        rbDepot.setSelected(true);
        panelVirement.setVisible(false);

        // Ajouter un écouteur sur le groupe de boutons
        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbVirement) {
                panelVirement.setVisible(true);
            } else {
                panelVirement.setVisible(false);
            }
        });

        // Charger les comptes du client
        loadComptes();

        // Écouteurs pour mettre à jour les soldes
        cbCompteSource.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherSoldeCompte(lblSoldeSource, newVal);
            }
        });

        cbCompteDestination.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherSoldeCompte(lblSoldeDestination, newVal);
            }
        });
    }

    private void loadClientInfo() {
        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                lblNomClient.setText(client.getNom() + " " + client.getPrenom());
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des informations client : " + e.getMessage());
        }
    }

    private void loadComptes() {
        try {
            // Récupérer les comptes du client connecté depuis la base de données
            List<Compte> comptes = compteRepository.findByClientId(clientId);

            if (comptes.isEmpty()) {
                Notification.notifWarning("Information", "Vous n'avez pas de comptes actifs.");
                return;
            }

            // Configuration des ComboBox
            cbCompteSource.setItems(FXCollections.observableArrayList(comptes));
            cbCompteSource.setCellFactory(param -> new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumero() + " - " + item.getType() + " - " +
                                NumberFormat.getCurrencyInstance(Locale.FRANCE).format(item.getSolde()));
                    }
                }
            });

            cbCompteSource.setButtonCell(new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumero() + " - " + item.getType());
                    }
                }
            });

            cbCompteDestination.setItems(FXCollections.observableArrayList(comptes));
            cbCompteDestination.setCellFactory(param -> new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumero() + " - " + item.getType() + " - " +
                                NumberFormat.getCurrencyInstance(Locale.FRANCE).format(item.getSolde()));
                    }
                }
            });

            cbCompteDestination.setButtonCell(new ListCell<Compte>() {
                @Override
                protected void updateItem(Compte item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumero() + " - " + item.getType());
                    }
                }
            });

            // Sélectionner le premier compte par défaut s'il existe
            if (!comptes.isEmpty()) {
                cbCompteSource.getSelectionModel().selectFirst();
                afficherSoldeCompte(lblSoldeSource, cbCompteSource.getValue());
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void afficherSoldeCompte(Label label, Compte compte) {
        if (compte != null) {
            label.setText(NumberFormat.getCurrencyInstance(Locale.FRANCE).format(compte.getSolde()));
        } else {
            label.setText("0 FCFA");
        }
    }

    @FXML
    private void handleValider(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(txtMontant.getText().trim());
        } catch (NumberFormatException e) {
            Notification.notifWarning("Erreur", "Montant invalide");
            return;
        }

        boolean success = false;

        // Créons un EntityManager pour gérer la transaction de persistance
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Transaction transaction = new Transaction();
            transaction.setMontant(montant);
            transaction.setDate(LocalDateTime.now());
            transaction.setStatut("COMPLETEE");

            // Description si fournie
            String description = txtDescription.getText();

            if (rbDepot.isSelected()) {
                // Dépôt
                Compte compte = em.find(Compte.class, cbCompteSource.getValue().getId());
                compte.setSolde(compte.getSolde() + montant);

                transaction.setType("DEPOT");
                transaction.setCompte(compte);

                em.persist(transaction);
                em.merge(compte);
                success = true;

            } else if (rbRetrait.isSelected()) {
                // Retrait
                Compte compte = em.find(Compte.class, cbCompteSource.getValue().getId());

                if (compte.getSolde() >= montant) {
                    compte.setSolde(compte.getSolde() - montant);

                    transaction.setType("RETRAIT");
                    transaction.setCompte(compte);

                    em.persist(transaction);
                    em.merge(compte);
                    success = true;
                } else {
                    em.getTransaction().rollback();
                    Notification.notifWarning("Erreur", "Solde insuffisant");
                    return;
                }

            } else if (rbVirement.isSelected()) {
                // Virement
                Compte compteSource = em.find(Compte.class, cbCompteSource.getValue().getId());
                Compte compteDestination = em.find(Compte.class, cbCompteDestination.getValue().getId());

                if (compteSource.getSolde() >= montant) {
                    compteSource.setSolde(compteSource.getSolde() - montant);
                    compteDestination.setSolde(compteDestination.getSolde() + montant);

                    transaction.setType("VIREMENT");
                    transaction.setCompteSource(compteSource);
                    transaction.setCompteDestination(compteDestination);

                    em.persist(transaction);
                    em.merge(compteSource);
                    em.merge(compteDestination);
                    success = true;
                } else {
                    em.getTransaction().rollback();
                    Notification.notifWarning("Erreur", "Solde insuffisant");
                    return;
                }
            }

            // Valider la transaction de persistance
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Notification.notifError("Erreur", "Erreur lors de la transaction : " + e.getMessage());
            e.printStackTrace();
            return;
        } finally {
            em.close();
        }

        if (success) {
            // Recharger les comptes pour afficher les soldes mis à jour
            loadComptes();

            // Notification de succès
            Notification.notifSuccess("Succès", "Transaction effectuée avec succès");

            // Réinitialiser le formulaire
            txtMontant.clear();
            txtDescription.clear();
        }
    }

    private boolean validateInputs() {
        if (cbCompteSource.getValue() == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner un compte source");
            return false;
        }

        if (txtMontant.getText().trim().isEmpty()) {
            Notification.notifWarning("Erreur", "Veuillez saisir un montant");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            if (montant <= 0) {
                Notification.notifWarning("Erreur", "Le montant doit être supérieur à zéro");
                return false;
            }

            if (rbRetrait.isSelected() || rbVirement.isSelected()) {
                if (montant > cbCompteSource.getValue().getSolde()) {
                    Notification.notifWarning("Erreur", "Solde insuffisant");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            Notification.notifWarning("Erreur", "Montant invalide");
            return false;
        }

        if (rbVirement.isSelected() && cbCompteDestination.getValue() == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner un compte destinataire");
            return false;
        }

        if (rbVirement.isSelected() && cbCompteSource.getValue().equals(cbCompteDestination.getValue())) {
            Notification.notifWarning("Erreur", "Les comptes source et destination sont identiques");
            return false;
        }

        return true;
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de Bord", "/fxml/client/dashboard.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger le tableau de bord: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCarte(ActionEvent event) {
        try {
            Outils.load(event, "Gestion de Carte Bancaire", "/fxml/client/carte.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCredit(ActionEvent event) {
        try {
            Outils.load(event, "Crédits et Simulations", "/fxml/client/credit.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleTicket(ActionEvent event) {
        try {
            Outils.load(event, "Support Client", "/fxml/client/ticket.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Effacer les informations de session
            SessionManager.logout();

            // Rediriger vers la page de connexion
            Outils.load(event, "Mini Système Bancaire - Connexion", "/fxml/Login.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de se déconnecter: " + ex.getMessage());
        }
    }
}