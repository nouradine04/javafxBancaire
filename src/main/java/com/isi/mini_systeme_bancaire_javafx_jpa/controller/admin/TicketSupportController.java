package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TicketSupportRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.TicketSupportServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.TicketSupportService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TicketSupportController implements Initializable {

    @FXML
    private TabPane tabPane;

    // Onglet liste des tickets
    @FXML
    private Tab tabTickets;

    @FXML
    private TextField txtSearchTicket;

    @FXML
    private ComboBox<String> cbFiltreStatut;

    @FXML
    private TableView<TicketSupport> tableTickets;

    @FXML
    private TableColumn<TicketSupport, Long> colId;

    @FXML
    private TableColumn<TicketSupport, String> colSujet;

    @FXML
    private TableColumn<TicketSupport, String> colClient;

    @FXML
    private TableColumn<TicketSupport, LocalDateTime> colDateOuverture;

    @FXML
    private TableColumn<TicketSupport, String> colStatut;

    @FXML
    private TableColumn<TicketSupport, String> colAdmin;

    @FXML
    private TableColumn<TicketSupport, Long> colTempsAttente;

    // Onglet détails du ticket
    @FXML
    private Label lblTicketId;

    @FXML
    private Label lblTicketSujet;

    @FXML
    private Label lblTicketClient;

    @FXML
    private Label lblTicketDateOuverture;

    @FXML
    private Label lblTicketStatut;

    @FXML
    private TextArea txtTicketDescription;

    @FXML
    private TextArea txtTicketCommentaire;

    @FXML
    private ComboBox<String> cbTicketStatut;

    @FXML
    private Button btnTicketPrendre;

    @FXML
    private Button btnTicketEnregistrer;

    @FXML
    private Button btnTicketResoudre;

    // Onglet nouveau ticket
    @FXML
    private Tab tabNouveauTicket;

    @FXML
    private TextField txtNouveauSujet;

    @FXML
    private TextArea txtNouveauDescription;

    @FXML
    private ComboBox<Client> cbNouveauClient;

    @FXML
    private ComboBox<String> cbNouveauStatut;

    @FXML
    private Button btnNouveauAnnuler;

    @FXML
    private Button btnNouveauEnregistrer;

    // Onglet statistiques
    @FXML
    private Tab tabStatistiques;

    @FXML
    private Label lblStatNbTicketsOuverts;

    @FXML
    private Label lblStatNbTicketsResolus;

    @FXML
    private Label lblStatTempsResolutionMoyen;

    @FXML
    private Button btnStatExportPDF;

    @FXML
    private Button btnStatExportExcel;

    private TicketSupportService ticketService = new TicketSupportServiceImpl();
    private TicketSupportRepository ticketRepository = new TicketSupportRepository();
    private ClientRepository clientRepository = new ClientRepository();

    private ObservableList<TicketSupport> ticketsList = FXCollections.observableArrayList();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private TicketSupport selectedTicket;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Vérifier si un administrateur est connecté
            if (!SessionManager.isAdminLoggedIn()) {
                Notification.notifError("Erreur", "Aucun administrateur connecté");
                return;
            }

            // Initialiser les statuts disponibles
            cbFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "Ouvert", "En cours", "Résolu", "Fermé"));
            cbFiltreStatut.setValue("Tous");

            cbTicketStatut.setItems(FXCollections.observableArrayList("Ouvert", "En cours", "Résolu", "Fermé"));

            cbNouveauStatut.setItems(FXCollections.observableArrayList("Ouvert", "En cours"));
            cbNouveauStatut.setValue("Ouvert");

            // Configurer le combobox des clients
            cbNouveauClient.setConverter(new javafx.util.StringConverter<Client>() {
                @Override
                public String toString(Client client) {
                    return client == null ? "" : client.getNom() + " " + client.getPrenom() + " (" + client.getEmail() + ")";
                }

                @Override
                public Client fromString(String s) {
                    return null; // Non utilisé
                }
            });

            // Configurer les colonnes du tableau
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));

            // Utiliser SimpleStringProperty au lieu de bindings pour éviter LazyInitializationException
            colClient.setCellValueFactory(cellData -> {
                TicketSupport ticket = cellData.getValue();
                String clientName = "N/A";
                try {
                    if (ticket != null && ticket.getClient() != null) {
                        Client client = ticket.getClient();
                        clientName = client.getNom() + " " + client.getPrenom();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'accès aux données client: " + e.getMessage());
                }
                return new SimpleStringProperty(clientName);
            });

            colDateOuverture.setCellValueFactory(new PropertyValueFactory<>("dateOuverture"));
            colDateOuverture.setCellFactory(column -> new TableCell<TicketSupport, LocalDateTime>() {
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

            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Utiliser SimpleStringProperty pour la colonne admin également
            colAdmin.setCellValueFactory(cellData -> {
                TicketSupport ticket = cellData.getValue();
                String adminName = "N/A";
                try {
                    if (ticket != null && ticket.getAdmin() != null) {
                        Admin admin = ticket.getAdmin();
                        adminName = admin.getUsername();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'accès aux données admin: " + e.getMessage());
                }
                return new SimpleStringProperty(adminName);
            });

            colTempsAttente.setCellValueFactory(cellData -> {
                TicketSupport ticket = cellData.getValue();
                long heures = 0;
                try {
                    if (ticket != null && ticket.getDateOuverture() != null) {
                        if (ticket.getDateFermeture() != null) {
                            // Si le ticket est fermé, calculer le temps de résolution
                            heures = java.time.Duration.between(ticket.getDateOuverture(), ticket.getDateFermeture()).toHours();
                        } else {
                            // Si le ticket est encore ouvert, calculer le temps d'attente
                            heures = java.time.Duration.between(ticket.getDateOuverture(), LocalDateTime.now()).toHours();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du calcul du temps d'attente: " + e.getMessage());
                }
                return new SimpleLongProperty(heures).asObject();
            });

            // Ajouter un listener pour la recherche
            txtSearchTicket.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrerTickets();
            });

            // Ajouter un listener pour le filtre de statut
            cbFiltreStatut.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                filtrerTickets();
            });

            // Ajouter un listener pour la sélection de ticket
            tableTickets.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    handleTicketSelection();
                }
            });

            // Charger les clients
            chargerClients();

            // Charger les tickets
            chargerTickets();

            // Mettre à jour les statistiques
            mettreAJourStatistiques();
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerClients() {
        try {
            // Récupérer tous les clients
            List<Client> clients = clientRepository.findAll();

            // Mettre à jour la liste observable
            clientsList.clear();
            clientsList.addAll(clients);

            // Mettre à jour le combobox
            cbNouveauClient.setItems(clientsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des clients : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerTickets() {
        try {
            // Récupérer tous les tickets
            List<TicketSupport> tickets = ticketRepository.findAll();

            // Mettre à jour la liste observable
            ticketsList.clear();
            ticketsList.addAll(tickets);

            // Appliquer les filtres
            filtrerTickets();
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des tickets : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filtrerTickets() {
        try {
            // Récupérer les valeurs des filtres
            String searchTerm = txtSearchTicket.getText().trim();
            String statut = cbFiltreStatut.getValue();

            // Filtrer les tickets
            ObservableList<TicketSupport> filteredList = FXCollections.observableArrayList();

            for (TicketSupport ticket : ticketsList) {
                boolean match = true;

                // Filtrer par terme de recherche
                if (!searchTerm.isEmpty()) {
                    boolean matchSearch = false;

                    // Vérifier si le terme de recherche est présent dans l'ID, le sujet ou le client
                    if (ticket.getId() != null && ticket.getId().toString().contains(searchTerm)) {
                        matchSearch = true;
                    } else if (ticket.getSujet() != null && ticket.getSujet().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matchSearch = true;
                    } else if (ticket.getClient() != null) {
                        try {
                            String clientInfo = ticket.getClient().getNom() + " " + ticket.getClient().getPrenom() + " " + ticket.getClient().getEmail();
                            if (clientInfo.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matchSearch = true;
                            }
                        } catch (Exception e) {
                            // Ignorer les erreurs d'accès aux propriétés lazy
                            System.err.println("Erreur lors du filtrage par client: " + e.getMessage());
                        }
                    }

                    match = matchSearch;
                }

                // Filtrer par statut
                if (match && !"Tous".equals(statut)) {
                    match = statut.equals(ticket.getStatut());
                }

                // Ajouter le ticket s'il correspond aux critères
                if (match) {
                    filteredList.add(ticket);
                }
            }

            // Mettre à jour le tableau
            tableTickets.setItems(filteredList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du filtrage des tickets : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mettreAJourStatistiques() {
        try {
            // Récupérer les statistiques
            int ticketsOuverts = ticketService.getNombreTicketsOuverts();
            int ticketsResolus = ticketService.getNombreTicketsResolus();
            double tempsResolutionMoyen = ticketService.getTempsResolutionMoyen();

            // Mettre à jour les labels
            lblStatNbTicketsOuverts.setText(String.valueOf(ticketsOuverts));
            lblStatNbTicketsResolus.setText(String.valueOf(ticketsResolus));
            lblStatTempsResolutionMoyen.setText(String.format("%.1f heures", tempsResolutionMoyen));
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour des statistiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTicketSelection() {
        try {
            // Récupérer le ticket sélectionné
            selectedTicket = tableTickets.getSelectionModel().getSelectedItem();

            if (selectedTicket != null) {
                // Recharger le ticket depuis la base de données pour éviter les problèmes de lazy loading
                Optional<TicketSupport> reloadedTicket = ticketRepository.findById(selectedTicket.getId());
                if (!reloadedTicket.isPresent()) {
                    Notification.notifWarning("Ticket", "Le ticket sélectionné n'existe plus");
                    return;
                }
                selectedTicket = reloadedTicket.get();

                // Remplir les champs avec les informations du ticket
                lblTicketId.setText(selectedTicket.getId().toString());
                lblTicketSujet.setText(selectedTicket.getSujet());

                try {
                    if (selectedTicket.getClient() != null) {
                        lblTicketClient.setText(selectedTicket.getClient().getNom() + " " + selectedTicket.getClient().getPrenom());
                    } else {
                        lblTicketClient.setText("N/A");
                    }
                } catch (Exception e) {
                    lblTicketClient.setText("N/A");
                    System.err.println("Erreur lors de l'accès aux données client: " + e.getMessage());
                }

                if (selectedTicket.getDateOuverture() != null) {
                    lblTicketDateOuverture.setText(selectedTicket.getDateOuverture().format(dateTimeFormatter));
                } else {
                    lblTicketDateOuverture.setText("N/A");
                }

                lblTicketStatut.setText(selectedTicket.getStatut());
                txtTicketDescription.setText(selectedTicket.getDescription());
                cbTicketStatut.setValue(selectedTicket.getStatut());

                // Activer/désactiver les boutons selon le statut du ticket
                btnTicketPrendre.setDisable(selectedTicket.getAdmin() != null);
                btnTicketResoudre.setDisable("Résolu".equals(selectedTicket.getStatut()) || "Fermé".equals(selectedTicket.getStatut()));

                // Changer l'onglet pour afficher les détails du ticket
                tabPane.getSelectionModel().select(1); // Onglet "Détails du Ticket"
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la sélection du ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrendreTicket(ActionEvent event) {
        try {
            // Vérifier qu'un ticket est sélectionné
            if (selectedTicket == null) {
                Notification.notifWarning("Ticket", "Veuillez sélectionner un ticket");
                return;
            }

            // Assigner le ticket à l'administrateur connecté
            Admin admin = SessionManager.getCurrentAdmin();
            boolean success = ticketService.assignerTicket(selectedTicket.getId(), admin.getId());

            if (success) {
                Notification.notifSuccess("Ticket", "Ticket pris en charge avec succès");

                // Mettre à jour le statut du ticket
                cbTicketStatut.setValue("En cours");

                // Mettre à jour le ticket
                selectedTicket.setAdmin(admin);
                selectedTicket.setStatut("En cours");

                // Désactiver le bouton "Prendre en charge"
                btnTicketPrendre.setDisable(true);

                // Recharger les tickets
                chargerTickets();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la prise en charge du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnregistrerTicket(ActionEvent event) {
        try {
            // Vérifier qu'un ticket est sélectionné
            if (selectedTicket == null) {
                Notification.notifWarning("Ticket", "Veuillez sélectionner un ticket");
                return;
            }

            // Mettre à jour le ticket
            selectedTicket.setStatut(cbTicketStatut.getValue());

            // Si un commentaire est saisi, l'ajouter à la description
            if (!txtTicketCommentaire.getText().isEmpty()) {
                String commentaire = "\n\n[" + LocalDateTime.now().format(dateTimeFormatter) +
                        " - " + SessionManager.getCurrentAdmin().getUsername() + "]\n" +
                        txtTicketCommentaire.getText();

                selectedTicket.setDescription(selectedTicket.getDescription() + commentaire);
                txtTicketDescription.setText(selectedTicket.getDescription());
                txtTicketCommentaire.clear();
            }

            // Si le statut est "Résolu" ou "Fermé", définir la date de fermeture
            if ("Résolu".equals(cbTicketStatut.getValue()) || "Fermé".equals(cbTicketStatut.getValue())) {
                selectedTicket.setDateFermeture(LocalDateTime.now());
            }

            // Enregistrer le ticket
            ticketRepository.save(selectedTicket);

            Notification.notifSuccess("Ticket", "Ticket mis à jour avec succès");

            // Recharger les tickets
            chargerTickets();

            // Mettre à jour les statistiques
            mettreAJourStatistiques();
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResoudreTicket(ActionEvent event) {
        try {
            // Vérifier qu'un ticket est sélectionné
            if (selectedTicket == null) {
                Notification.notifWarning("Ticket", "Veuillez sélectionner un ticket");
                return;
            }

            // Si un commentaire est saisi, l'utiliser comme commentaire de résolution
            String commentaire = txtTicketCommentaire.getText().isEmpty() ?
                    "Ticket résolu." :
                    txtTicketCommentaire.getText();

            // Résoudre le ticket
            boolean success = ticketService.cloturerTicket(selectedTicket.getId(), commentaire);

            if (success) {
                Notification.notifSuccess("Ticket", "Ticket résolu avec succès");

                // Mettre à jour le statut du ticket dans l'interface
                lblTicketStatut.setText("Résolu");
                cbTicketStatut.setValue("Résolu");

                // Désactiver le bouton "Résoudre"
                btnTicketResoudre.setDisable(true);

                // Vider le champ de commentaire
                txtTicketCommentaire.clear();

                // Recharger les tickets
                chargerTickets();

                // Mettre à jour les statistiques
                mettreAJourStatistiques();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la résolution du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNouveauTicket(ActionEvent event) {
        // Changer l'onglet pour afficher le formulaire de nouveau ticket
        tabPane.getSelectionModel().select(tabNouveauTicket);
    }

    @FXML
    private void handleAnnulerNouveauTicket(ActionEvent event) {
        // Réinitialiser les champs
        txtNouveauSujet.clear();
        txtNouveauDescription.clear();
        cbNouveauClient.setValue(null);
        cbNouveauStatut.setValue("Ouvert");

        // Retourner à l'onglet de liste des tickets
        tabPane.getSelectionModel().select(tabTickets);
    }

    @FXML
    private void handleEnregistrerNouveauTicket(ActionEvent event) {
        try {
            // Vérifier que tous les champs obligatoires sont remplis
            if (txtNouveauSujet.getText().isEmpty() || txtNouveauDescription.getText().isEmpty() || cbNouveauClient.getValue() == null) {
                Notification.notifWarning("Ticket", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Créer un nouveau ticket
            TicketSupport ticket = new TicketSupport();
            ticket.setSujet(txtNouveauSujet.getText());
            ticket.setDescription(txtNouveauDescription.getText());
            ticket.setClient(cbNouveauClient.getValue());
            ticket.setStatut(cbNouveauStatut.getValue());
            ticket.setDateOuverture(LocalDateTime.now());

            // Si l'administrateur prend en charge le ticket, l'assigner
            if ("En cours".equals(cbNouveauStatut.getValue())) {
                ticket.setAdmin(SessionManager.getCurrentAdmin());
            }

            // Enregistrer le ticket
            ticket = ticketRepository.save(ticket);

            if (ticket != null) {
                Notification.notifSuccess("Ticket", "Ticket créé avec succès");

                // Réinitialiser les champs
                handleAnnulerNouveauTicket(event);

                // Recharger les tickets
                chargerTickets();

                // Mettre à jour les statistiques
                mettreAJourStatistiques();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la création du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        Notification.notifInfo("Export", "Fonctionnalité d'export PDF à implémenter");
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        Notification.notifInfo("Export", "Fonctionnalité d'export Excel à implémenter");
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
            e.printStackTrace();
        }
    }
}