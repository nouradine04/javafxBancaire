package com.isi.mini_systeme_bancaire_javafx_jpa.utils;



import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.util.Optional;

public class Notification {

    public static void notifSuccess(String titre, String message) {
        NotificationType type = NotificationType.SUCCESS;
        TrayNotification tray = new TrayNotification();
        tray.setTitle(titre);
        tray.setMessage(message);
        tray.setNotificationType(type);
       // tray.showAndDismiss(Duration.seconds(2));
    }

    public static void notifError(String titre, String message) {
        NotificationType type = NotificationType.ERROR;
        TrayNotification tray = new TrayNotification();
        tray.setTitle(titre);
        tray.setMessage(message);
        tray.setNotificationType(type);
       // tray.showAndDismiss(Duration.seconds(2));
    }

    public static void notifInfo(String titre, String message) {
        NotificationType type = NotificationType.INFORMATION;
        TrayNotification tray = new TrayNotification();
        tray.setTitle(titre);
        tray.setMessage(message);
        tray.setNotificationType(type);
       // tray.showAndDismiss(Duration.seconds(2));
    }

    public static void notifWarning(String titre, String message) {
        NotificationType type = NotificationType.WARNING;
        TrayNotification tray = new TrayNotification();
        tray.setTitle(titre);
        tray.setMessage(message);
        tray.setNotificationType(type);
       // tray.showAndDismiss(Duration.seconds(2));
    }

    public static boolean showConfirmationDialog(String title, String header, String content) {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle(title);
        confirmationDialog.setHeaderText(header);
        confirmationDialog.setContentText(content);

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean confirmDelete() {
        return
                showConfirmationDialog(
                "Confirmation de suppression",
                "Voulez-vous vraiment effectuer cette suppression ?",
                "Cette action est irréversible."
        );
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        //alert.showAndWait();
    }
}