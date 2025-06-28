package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Outils {
    private void loadPage(ActionEvent event, String title, String url) throws IOException {
        ((Node) event.getSource()).getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(url));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public static void load(ActionEvent event, String title, String url) throws IOException {
        new Outils().loadPage(event, title, url);
    }

    // Méthode pour ouvrir une nouvelle fenêtre sans fermer la fenêtre actuelle
    public static void openNewWindow(String title, String url) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Outils.class.getResource(url));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    // Méthode pour charger un contrôleur FXML et le retourner
    public static <T> T loadController(String url) throws IOException {
        FXMLLoader loader = new FXMLLoader(Outils.class.getResource(url));
        loader.load();
        return loader.getController();
    }

    // Méthode pour générer un numéro de compte unique
    public static String generateAccountNumber() {
        StringBuilder sb = new StringBuilder("BQ");
        for (int i = 0; i < 10; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    // Méthode pour générer un numéro de carte bancaire
    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    // Méthode pour générer un code CVV
    public static String generateCVV() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}