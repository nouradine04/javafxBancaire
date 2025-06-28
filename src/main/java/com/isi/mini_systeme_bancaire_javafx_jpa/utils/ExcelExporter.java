package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporte les transactions vers un fichier Excel avec des informations générales
     * @param transactions Liste des transactions à exporter
     * @param emetteur Nom de l'émetteur du rapport
     * @param titre Titre du rapport
     * @param filePath Chemin du fichier de destination
     * @return true si l'export a réussi, false sinon
     */
    public static boolean exporterTransactions(List<Transaction> transactions, String emetteur, String titre, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Création de la feuille des transactions
            Sheet sheet = workbook.createSheet("Transactions");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Titre
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titre);
            titleCell.setCellStyle(titleStyle);

            // Émetteur
            Row emetteurRow = sheet.createRow(1);
            emetteurRow.createCell(0).setCellValue("Émis par : " + emetteur);

            // Date d'émission
            Row dateRow = sheet.createRow(2);
            dateRow.createCell(0).setCellValue("Date d'émission : " + LocalDateTime.now().format(dateFormatter));

            // En-têtes des colonnes
            String[] headers = {"Date", "Type", "Montant", "Client", "Compte", "Statut"};
            Row headerRow = sheet.createRow(4);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Données des transactions
            int rowNum = 5;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);

                // Date
                row.createCell(0).setCellValue(transaction.getDate().format(dateFormatter));

                // Type
                row.createCell(1).setCellValue(transaction.getType());

                // Montant
                row.createCell(2).setCellValue(transaction.getMontant());

                // Client
                String clientName = transaction.getCompte() != null &&
                        transaction.getCompte().getClient() != null ?
                        transaction.getCompte().getClient().getNom() + " " +
                                transaction.getCompte().getClient().getPrenom() : "";
                row.createCell(3).setCellValue(clientName);

                // Compte
                String compteNumero = transaction.getCompte() != null ?
                        transaction.getCompte().getNumero() : "";
                row.createCell(4).setCellValue(compteNumero);

                // Statut
                row.createCell(5).setCellValue(transaction.getStatut());
            }

            // Ajustement de la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écriture du fichier
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}