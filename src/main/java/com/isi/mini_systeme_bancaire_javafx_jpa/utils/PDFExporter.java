package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static boolean exporterTransactions(List<Transaction> transactions, String emetteur, String titre, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 770);
                contentStream.showText(titre);
                contentStream.endText();

                // Émetteur
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Émis par : " + emetteur);
                contentStream.endText();

                // Date d'émission
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("Date d'émission : " + LocalDateTime.now().format(dateFormatter));
                contentStream.endText();

                // En-têtes de colonnes
                String[] headers = {"Date", "Type", "Montant", "Client", "Compte", "Statut"};
                float[] colWidths = {100, 80, 80, 100, 80, 80};
                float y = 700;
                float x = 50;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                for (int i = 0; i < headers.length; i++) {
                    contentStream.newLineAtOffset(i == 0 ? x : colWidths[i-1], i == 0 ? y : 0);
                    contentStream.showText(headers[i]);
                }
                contentStream.endText();

                // Ligne de séparation
                contentStream.moveTo(50, y - 5);
                contentStream.lineTo(550, y - 5);
                contentStream.stroke();

                // Données des transactions
                y -= 20;
                for (Transaction transaction : transactions) {
                    if (y < 50) {
                        // Nouvelle page si pas assez d'espace
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        PDPageContentStream newContentStream = new PDPageContentStream(document, page);
                        y = 770;
                        newContentStream.close();
                        continue;
                    }

                    x = 50;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(transaction.getDate().format(dateFormatter));
                    contentStream.endText();

                    x += colWidths[0];
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(transaction.getType());
                    contentStream.endText();

                    x += colWidths[1];
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(String.format("%.2f FCFA", transaction.getMontant()));
                    contentStream.endText();

                    x += colWidths[2];
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    String clientName = transaction.getCompte() != null &&
                            transaction.getCompte().getClient() != null ?
                            transaction.getCompte().getClient().getNom() + " " +
                                    transaction.getCompte().getClient().getPrenom() : "";
                    contentStream.showText(clientName);
                    contentStream.endText();

                    x += colWidths[3];
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    String compteNumero = transaction.getCompte() != null ?
                            transaction.getCompte().getNumero() : "";
                    contentStream.showText(compteNumero);
                    contentStream.endText();

                    x += colWidths[4];
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(transaction.getStatut());
                    contentStream.endText();

                    y -= 15;
                }

                // Pied de page
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(50, 30);
                contentStream.showText("Document généré automatiquement - Mini Système Bancaire");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(50, 20);
                contentStream.showText("Page 1/" + document.getNumberOfPages());
                contentStream.endText();
            }

            document.save(filePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}