package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {

    public List<Transaction> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t " +
                            "LEFT JOIN FETCH t.compte c " +
                            "LEFT JOIN FETCH c.client " +
                            "LEFT JOIN FETCH t.compteSource cs " +
                            "LEFT JOIN FETCH t.compteDestination cd " +
                            "ORDER BY t.date DESC",
                    Transaction.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Transaction> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t " +
                            "LEFT JOIN FETCH t.compte c " +
                            "LEFT JOIN FETCH c.client " +
                            "LEFT JOIN FETCH t.compteSource cs " +
                            "LEFT JOIN FETCH t.compteDestination cd " +
                            "WHERE t.id = :id",
                    Transaction.class
            );
            query.setParameter("id", id);
            List<Transaction> transactions = query.getResultList();
            return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.get(0));
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByCompteId(Long compteId) {
        if (compteId == null) {
            System.err.println("CompteId est null dans findByCompteId");
            return new ArrayList<>();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Requête simplifiée pour éviter les problèmes de jointure
            String jpql = "SELECT t FROM Transaction t WHERE "
                    + "t.compte.id = :compteId OR "
                    + "t.compteSource.id = :compteId OR "
                    + "t.compteDestination.id = :compteId "
                    + "ORDER BY t.date DESC";

            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("compteId", compteId);

            List<Transaction> results = query.getResultList();

            // Forcer l'initialisation des objets associés pour éviter LazyInitializationException
            for (Transaction t : results) {
                if (t.getCompte() != null) {
                    t.getCompte().getId(); // Force initialization
                }
                if (t.getCompteSource() != null) {
                    t.getCompteSource().getId();
                }
                if (t.getCompteDestination() != null) {
                    t.getCompteDestination().getId();
                }
            }

            System.out.println("Transactions trouvées: " + results.size() + " pour le compte ID: " + compteId);
            return results;
        } catch (Exception e) {
            System.err.println("Erreur dans findByCompteId: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByCompteIdNative(Long compteId) {
        if (compteId == null) {
            return new ArrayList<>();
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createNativeQuery(
                    "SELECT * FROM transactions WHERE compte_id = ? OR compte_source_id = ? OR compte_dest_id = ?",
                    Transaction.class);
            query.setParameter(1, compteId);
            query.setParameter(2, compteId);
            query.setParameter(3, compteId);
            List<Transaction> results = query.getResultList();
            System.out.println("SQL direct: " + results.size() + " transactions trouvées pour le compte ID: " + compteId);
            return results;
        } catch (Exception e) {
            System.err.println("Erreur dans findByCompteIdNative: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Transaction save(Transaction transaction) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (transaction.getId() == null) {
                em.persist(transaction);
            } else {
                transaction = em.merge(transaction);
            }
            em.getTransaction().commit();
            return transaction;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Transaction transaction) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(transaction)) {
                transaction = em.merge(transaction);
            }
            em.remove(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Transaction transaction = em.find(Transaction.class, id);
            if (transaction != null) {
                em.remove(transaction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}