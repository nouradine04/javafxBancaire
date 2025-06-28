package com.isi.mini_systeme_bancaire_javafx_jpa;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Vérifier/créer l'administrateur par défaut
        createDefaultAdmin();

        // Charger la vue de login
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        primaryStage.setTitle("Mini Système Bancaire - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Crée un administrateur par défaut s'il n'en existe pas déjà un
     */
    private void createDefaultAdmin() {
        AdminRepository adminRepository = new AdminRepository();

        // Vérifier si un administrateur existe déjà
        if (adminRepository.findAll().isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ROLE_ADMIN");

            adminRepository.save(admin);
            System.out.println("Administrateur par défaut créé: admin/admin123");
        }
    }

    /**
     * Méthode principale pour démarrer l'application
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        // Fermer la connexion à la base de données
        JpaUtil.close();
    }
}
