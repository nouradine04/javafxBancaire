package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {
    private static final String FROM_EMAIL = "aliou.18.ndour@gmail.com";
    private static final String FROM_PASSWORD = "lzgt jtok qwld jfsx";
    private static final String SUBJECT_PREFIX = "Mini Système Bancaire";

    public static void sendMail(String email, String username, String message) {
        String toEmail = email;
        String subject = SUBJECT_PREFIX;
        String fullMessage = username + " " + message;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(FROM_EMAIL));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(fullMessage);
            Transport.send(mimeMessage);

            Notification.notifSuccess("Email", "Email envoyé avec succès à " + toEmail);
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.notifError("Erreur", "Échec d'envoi d'email: " + ex.getMessage());
        }
    }

    public static void sendNotificationTransaction(String email, String nom, String type, double montant, String numero) {
        String subject = SUBJECT_PREFIX + " - Transaction " + type;
        String message = "Bonjour " + nom + ",\n\n"
                + "Nous vous informons qu'une transaction de type " + type + " d'un montant de "
                + montant + " FCFA a été effectuée sur votre compte N° " + numero + ".\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendWelcomeEmail(String email, String nom, String numero) {
        String subject = SUBJECT_PREFIX + " - Bienvenue";
        String message = "Bonjour " + nom + ",\n\n"
                + "Bienvenue dans notre système bancaire !\n"
                + "Votre compte N° " + numero + " a été créé avec succès.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendCustomEmail(String email, String subject, String message) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(FROM_EMAIL));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Transport.send(mimeMessage);

            Notification.notifSuccess("Email", "Email envoyé avec succès à " + email);
        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.notifError("Erreur", "Échec d'envoi d'email: " + ex.getMessage());
        }
    }

    // Méthodes supplémentaires pour couvrir toutes les notifications du cahier des charges

    public static void sendTicketCreationNotification(String email, String nom, String numTicket, String sujet) {
        String subject = SUBJECT_PREFIX + " - Ticket créé";
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre ticket N° " + numTicket + " concernant \"" + sujet + "\" a été créé avec succès.\n"
                + "Un de nos agents vous contactera dans les plus brefs délais.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendTicketStatusNotification(String email, String nom, String numTicket, String nouveauStatut) {
        String subject = SUBJECT_PREFIX + " - Mise à jour de votre ticket";
        String message = "Bonjour " + nom + ",\n\n"
                + "Le statut de votre ticket N° " + numTicket + " a été mis à jour : " + nouveauStatut + ".\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendCardCreationNotification(String email, String nom, String numeroMasque) {
        String subject = SUBJECT_PREFIX + " - Carte bancaire créée";
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre carte bancaire N° " + numeroMasque + " a été créée avec succès.\n"
                + "Vous recevrez votre carte et votre code PIN par courrier postal dans les prochains jours.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendCardBlockedNotification(String email, String nom, String numeroMasque) {
        String subject = SUBJECT_PREFIX + " - Carte bancaire bloquée";
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre carte bancaire N° " + numeroMasque + " a été bloquée.\n"
                + "Si vous n'êtes pas à l'origine de cette action, veuillez contacter notre service client immédiatement.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendCardUnblockedNotification(String email, String nom, String numeroMasque) {
        String subject = SUBJECT_PREFIX + " - Carte bancaire débloquée";
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre carte bancaire N° " + numeroMasque + " a été débloquée et est à nouveau utilisable.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendCreditApprovalNotification(String email, String nom, double montant, double mensualite, int duree) {
        String subject = SUBJECT_PREFIX + " - Crédit approuvé";
        String message = "Bonjour " + nom + ",\n\n"
                + "Nous avons le plaisir de vous informer que votre demande de crédit a été approuvée.\n"
                + "Montant: " + montant + " FCFA\n"
                + "Durée: " + duree + " mois\n"
                + "Mensualité: " + mensualite + " FCFA\n\n"
                + "Le montant sera prochainement versé sur votre compte.\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendFraisBancairesNotification(String email, String nom, String typeOperation, double montant, String numero) {
        String subject = SUBJECT_PREFIX + " - Frais bancaires";
        String message = "Bonjour " + nom + ",\n\n"
                + "Des frais bancaires de " + montant + " FCFA ont été appliqués à votre compte N° " + numero
                + " pour l'opération suivante: " + typeOperation + ".\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    public static void sendPlacementFinancierNotification(String email, String nom, double montant,
                                                          double interet, int duree, String typeInvestissement) {
        String subject = SUBJECT_PREFIX + " - Placement financier";
        String message = "Bonjour " + nom + ",\n\n"
                + "Votre placement financier a été enregistré avec succès.\n"
                + "Type d'investissement: " + typeInvestissement + "\n"
                + "Montant investi: " + montant + " FCFA\n"
                + "Durée: " + duree + " mois\n"
                + "Taux d'intérêt annuel: " + interet + "%\n"
                + "Rendement estimé: " + (montant * (interet/100) * (duree/12)) + " FCFA\n\n"
                + "Cordialement,\n"
                + "L'équipe du Mini Système Bancaire";

        sendCustomEmail(email, subject, message);
    }

    // Méthode pour masquer le numéro de carte (pratique courante pour la sécurité)
    public static String masquerNumeroCarte(String numero) {
        if (numero == null || numero.length() < 8) {
            return "XXXX-XXXX-XXXX-XXXX";
        }
        return "XXXX-XXXX-XXXX-" + numero.substring(numero.length() - 4);
    }
}
