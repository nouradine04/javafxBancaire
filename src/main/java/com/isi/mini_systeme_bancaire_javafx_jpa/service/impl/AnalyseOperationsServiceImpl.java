package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.AnalyseOperationsService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.ExcelExporter;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.PDFExporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyseOperationsServiceImpl implements AnalyseOperationsService {

    private final TransactionRepository transactionRepository;

    // Constantes pour les seuils de détection par défaut
    private static final double SEUIL_MONTANT_ELEVE = 1000000.0; // 1 million FCFA
    private static final int NOMBRE_MIN_TRANSACTIONS = 5;
    private static final int INTERVALLE_MINUTES = 60; // 1 heure
    private static final int DEBUT_HEURE_NORMALE = 8; // 8h du matin
    private static final int FIN_HEURE_NORMALE = 18; // 18h du soir

    public AnalyseOperationsServiceImpl() {
        this.transactionRepository = new TransactionRepository();
    }

    @Override
    public List<Transaction> detecterTransactionsSuspectes() {
        // Combiner les résultats de différentes méthodes de détection
        Set<Transaction> transactions = new HashSet<>();

        transactions.addAll(detecterTransactionsMontantEleve(SEUIL_MONTANT_ELEVE));
        transactions.addAll(detecterTransactionsMultiples(NOMBRE_MIN_TRANSACTIONS, INTERVALLE_MINUTES));
        transactions.addAll(detecterTransactionsHeuresInhabituelles(DEBUT_HEURE_NORMALE, FIN_HEURE_NORMALE));

        // Ajouter les transactions de type smurfing
        Map<Long, List<Transaction>> transactionsSmurfing =
                detecterMotifSmurfing(3, 50000.0, 500000.0, 1440); // 3 petites transactions < 50k, suivies d'une grosse > 500k, dans un délai de 24h

        for (List<Transaction> list : transactionsSmurfing.values()) {
            transactions.addAll(list);
        }

        // Trier par date décroissante
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> detecterTransactionsMontantEleve(double seuil) {
        List<Transaction> toutes = transactionRepository.findAll();

        return toutes.stream()
                .filter(t -> t.getMontant() > seuil)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> detecterTransactionsMultiples(int nombreMin, int intervalleMins) {
        List<Transaction> toutes = transactionRepository.findAll();
        List<Transaction> suspectes = new ArrayList<>();

        // Grouper les transactions par compte
        Map<Long, List<Transaction>> parCompte = toutes.stream()
                .filter(t -> t.getCompte() != null)
                .collect(Collectors.groupingBy(t -> t.getCompte().getId()));

        // Pour chaque compte, analyser les transactions
        for (List<Transaction> transactions : parCompte.values()) {
            // Trier par date
            transactions.sort(Comparator.comparing(Transaction::getDate));

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t1 = transactions.get(i);
                LocalDateTime debut = t1.getDate();
                LocalDateTime fin = debut.plusMinutes(intervalleMins);

                // Compter le nombre de transactions dans l'intervalle
                int count = 1;
                List<Transaction> groupeTransactions = new ArrayList<>();
                groupeTransactions.add(t1);

                for (int j = i + 1; j < transactions.size(); j++) {
                    Transaction t2 = transactions.get(j);
                    if (t2.getDate().isAfter(debut) && !t2.getDate().isAfter(fin)) {
                        count++;
                        groupeTransactions.add(t2);
                    } else if (t2.getDate().isAfter(fin)) {
                        break;
                    }
                }

                // Si le nombre dépasse le seuil, ajouter toutes ces transactions
                if (count >= nombreMin) {
                    suspectes.addAll(groupeTransactions);
                    // Avancer l'index à la fin du groupe
                    i += count - 1;
                }
            }
        }

        return suspectes;
    }

    @Override
    public List<Transaction> detecterTransactionsHeuresInhabituelles(int debutHeureNormale, int finHeureNormale) {
        List<Transaction> toutes = transactionRepository.findAll();

        return toutes.stream()
                .filter(t -> {
                    int heure = t.getDate().getHour();
                    return heure < debutHeureNormale || heure > finHeureNormale;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<Transaction>> detecterMotifSmurfing(int nombrePetites, double seuilPetite, double seuilGrosse, int intervalleMins) {
        List<Transaction> toutes = transactionRepository.findAll();
        Map<Long, List<Transaction>> resultats = new HashMap<>();

        // Grouper les transactions par client
        Map<Long, List<Transaction>> parClient = toutes.stream()
                .filter(t -> t.getCompte() != null && t.getCompte().getClient() != null)
                .collect(Collectors.groupingBy(t -> t.getCompte().getClient().getId()));

        // Pour chaque client, analyser les transactions
        for (Map.Entry<Long, List<Transaction>> entry : parClient.entrySet()) {
            Long clientId = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            // Trier par date
            transactions.sort(Comparator.comparing(Transaction::getDate));

            for (int i = 0; i < transactions.size(); i++) {
                // Chercher une grosse transaction
                Transaction grosseTransaction = transactions.get(i);
                if (grosseTransaction.getMontant() < seuilGrosse) {
                    continue;
                }

                // Chercher des petites transactions précédentes dans l'intervalle
                LocalDateTime fin = grosseTransaction.getDate();
                LocalDateTime debut = fin.minusMinutes(intervalleMins);

                List<Transaction> petitesTransactions = transactions.stream()
                        .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin) && t.getMontant() <= seuilPetite)
                        .collect(Collectors.toList());

                // Si le nombre de petites transactions est suffisant, c'est suspect
                if (petitesTransactions.size() >= nombrePetites) {
                    List<Transaction> groupe = new ArrayList<>(petitesTransactions);
                    groupe.add(grosseTransaction);
                    resultats.put(clientId, groupe);
                    break;
                }
            }
        }

        return resultats;
    }

    @Override
    public String genererRapportAnalyse(LocalDateTime debut, LocalDateTime fin) {
        StringBuilder rapport = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        rapport.append("RAPPORT D'ANALYSE DES TRANSACTIONS SUSPECTES\n");
        rapport.append("==============================================\n\n");
        rapport.append("Période d'analyse: du ").append(debut.format(formatter))
                .append(" au ").append(fin.format(formatter)).append("\n\n");

        // Transactions de montant élevé
        List<Transaction> montantEleve = detecterTransactionsMontantEleve(SEUIL_MONTANT_ELEVE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList());

        rapport.append("1. TRANSACTIONS DE MONTANT ÉLEVÉ (> ").append(SEUIL_MONTANT_ELEVE).append(" FCFA)\n");
        rapport.append("   Nombre de transactions détectées: ").append(montantEleve.size()).append("\n\n");
        for (Transaction t : montantEleve) {
            rapport.append("   - ").append(t.getDate().format(formatter))
                    .append(" | Type: ").append(t.getType())
                    .append(" | Montant: ").append(t.getMontant()).append(" FCFA");

            if (t.getCompte() != null && t.getCompte().getClient() != null) {
                Client client = t.getCompte().getClient();
                rapport.append(" | Client: ").append(client.getNom()).append(" ").append(client.getPrenom());
            }

            rapport.append("\n");
        }
        rapport.append("\n");

        // Transactions multiples dans un intervalle court
        List<Transaction> multiples = detecterTransactionsMultiples(NOMBRE_MIN_TRANSACTIONS, INTERVALLE_MINUTES).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList());

        rapport.append("2. TRANSACTIONS MULTIPLES (>= ").append(NOMBRE_MIN_TRANSACTIONS)
                .append(" transactions en ").append(INTERVALLE_MINUTES).append(" minutes)\n");
        rapport.append("   Nombre de transactions détectées: ").append(multiples.size()).append("\n\n");

        // Organiser par compte
        Map<Long, List<Transaction>> multiplesByCompte = multiples.stream()
                .filter(t -> t.getCompte() != null)
                .collect(Collectors.groupingBy(t -> t.getCompte().getId()));

        for (Map.Entry<Long, List<Transaction>> entry : multiplesByCompte.entrySet()) {
            List<Transaction> transactionsCompte = entry.getValue();
            Transaction premiereTransaction = transactionsCompte.get(0);

            if (premiereTransaction.getCompte() != null) {
                rapport.append("   Compte: ").append(premiereTransaction.getCompte().getNumero());

                if (premiereTransaction.getCompte().getClient() != null) {
                    Client client = premiereTransaction.getCompte().getClient();
                    rapport.append(" | Client: ").append(client.getNom()).append(" ").append(client.getPrenom());
                }

                rapport.append("\n");

                for (Transaction t : transactionsCompte) {
                    rapport.append("     - ").append(t.getDate().format(formatter))
                            .append(" | Type: ").append(t.getType())
                            .append(" | Montant: ").append(t.getMontant()).append(" FCFA\n");
                }

                rapport.append("\n");
            }
        }

        // Transactions à des heures inhabituelles
        List<Transaction> heuresInhabituelles = detecterTransactionsHeuresInhabituelles(DEBUT_HEURE_NORMALE, FIN_HEURE_NORMALE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList());

        rapport.append("3. TRANSACTIONS À DES HEURES INHABITUELLES (en dehors de ").append(DEBUT_HEURE_NORMALE)
                .append("h-").append(FIN_HEURE_NORMALE).append("h)\n");
        rapport.append("   Nombre de transactions détectées: ").append(heuresInhabituelles.size()).append("\n\n");
        for (Transaction t : heuresInhabituelles) {
            rapport.append("   - ").append(t.getDate().format(formatter))
                    .append(" | Type: ").append(t.getType())
                    .append(" | Montant: ").append(t.getMontant()).append(" FCFA");

            if (t.getCompte() != null && t.getCompte().getClient() != null) {
                Client client = t.getCompte().getClient();
                rapport.append(" | Client: ").append(client.getNom()).append(" ").append(client.getPrenom());
            }

            rapport.append("\n");
        }
        rapport.append("\n");

        // Motifs de smurfing
        Map<Long, List<Transaction>> smurfing = detecterMotifSmurfing(3, 50000.0, 500000.0, 1440);
        rapport.append("4. MOTIFS DE SMURFING (plusieurs petites transactions suivies d'une grosse)\n");
        rapport.append("   Nombre de clients concernés: ").append(smurfing.size()).append("\n\n");

        for (Map.Entry<Long, List<Transaction>> entry : smurfing.entrySet()) {
            Long clientId = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            // Trouver la grosse transaction (dernière de la liste)
            Transaction grosseTransaction = transactions.get(transactions.size() - 1);
            Client client = null;

            if (grosseTransaction.getCompte() != null && grosseTransaction.getCompte().getClient() != null) {
                client = grosseTransaction.getCompte().getClient();
                rapport.append("   Client: ").append(client.getNom()).append(" ").append(client.getPrenom()).append("\n");
            } else {
                rapport.append("   Client ID: ").append(clientId).append("\n");
            }

            rapport.append("     Petites transactions:\n");
            for (int i = 0; i < transactions.size() - 1; i++) {
                Transaction t = transactions.get(i);
                rapport.append("     - ").append(t.getDate().format(formatter))
                        .append(" | Type: ").append(t.getType())
                        .append(" | Montant: ").append(t.getMontant()).append(" FCFA\n");
            }

            rapport.append("     Grosse transaction:\n");
            rapport.append("     - ").append(grosseTransaction.getDate().format(formatter))
                    .append(" | Type: ").append(grosseTransaction.getType())
                    .append(" | Montant: ").append(grosseTransaction.getMontant()).append(" FCFA\n\n");
        }

        rapport.append("\nRAPPORT GÉNÉRÉ LE: ").append(LocalDateTime.now().format(formatter));

        return rapport.toString();
    }

    @Override
    public boolean exporterRapportPDF(LocalDateTime debut, LocalDateTime fin, String filePath) {
        // Cette méthode nécessite de créer une classe spécifique pour exporter le rapport en PDF
        // Pour l'instant, nous allons utiliser une approche simple en adaptant notre outil d'export existant

        // Récupérer toutes les transactions suspectes
        List<Transaction> suspectes = new ArrayList<>();

        // Transactions de montant élevé
        suspectes.addAll(detecterTransactionsMontantEleve(SEUIL_MONTANT_ELEVE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Transactions multiples
        suspectes.addAll(detecterTransactionsMultiples(NOMBRE_MIN_TRANSACTIONS, INTERVALLE_MINUTES).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Transactions à des heures inhabituelles
        suspectes.addAll(detecterTransactionsHeuresInhabituelles(DEBUT_HEURE_NORMALE, FIN_HEURE_NORMALE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Enlever les doublons
        suspectes = suspectes.stream()
                .distinct()
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        // Utiliser notre exporteur PDF pour générer le rapport
        return PDFExporter.exporterTransactions(suspectes, "ADMIN", "RAPPORT_ANALYSE", filePath);
    }

    @Override
    public boolean exporterRapportExcel(LocalDateTime debut, LocalDateTime fin, String filePath) {
        // Récupérer toutes les transactions suspectes
        List<Transaction> suspectes = new ArrayList<>();

        // Transactions de montant élevé
        suspectes.addAll(detecterTransactionsMontantEleve(SEUIL_MONTANT_ELEVE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Transactions multiples
        suspectes.addAll(detecterTransactionsMultiples(NOMBRE_MIN_TRANSACTIONS, INTERVALLE_MINUTES).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Transactions à des heures inhabituelles
        suspectes.addAll(detecterTransactionsHeuresInhabituelles(DEBUT_HEURE_NORMALE, FIN_HEURE_NORMALE).stream()
                .filter(t -> t.getDate().isAfter(debut) && t.getDate().isBefore(fin))
                .collect(Collectors.toList()));

        // Enlever les doublons
        suspectes = suspectes.stream()
                .distinct()
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        // Utiliser notre exporteur Excel pour générer le rapport
        return ExcelExporter.exporterTransactions(suspectes, "ADMIN", "RAPPORT_ANALYSE", filePath);
    }
}
