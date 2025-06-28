package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.TicketSupport;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TicketSupportRepository {

    public List<TicketSupport> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "LEFT JOIN FETCH t.admin a " +
                            "ORDER BY t.dateOuverture DESC",
                    TicketSupport.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<TicketSupport> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "LEFT JOIN FETCH t.admin a " +
                            "WHERE t.id = :id",
                    TicketSupport.class);
            query.setParameter("id", id);
            List<TicketSupport> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<TicketSupport> findByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.admin a " +
                            "WHERE t.client.id = :clientId " +
                            "ORDER BY t.dateOuverture DESC",
                    TicketSupport.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<TicketSupport> findByAdminId(Long adminId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "WHERE t.admin.id = :adminId " +
                            "ORDER BY t.dateOuverture DESC",
                    TicketSupport.class);
            query.setParameter("adminId", adminId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<TicketSupport> findByStatut(String statut) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "LEFT JOIN FETCH t.admin a " +
                            "WHERE t.statut = :statut " +
                            "ORDER BY t.dateOuverture DESC",
                    TicketSupport.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<TicketSupport> findTicketsNonAttribues() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "WHERE t.admin IS NULL " +
                            "ORDER BY t.dateOuverture ASC",
                    TicketSupport.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<TicketSupport> searchTickets(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<TicketSupport> query = em.createQuery(
                    "SELECT DISTINCT t FROM TicketSupport t " +
                            "LEFT JOIN FETCH t.client c " +
                            "LEFT JOIN FETCH t.admin a " +
                            "WHERE LOWER(t.sujet) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(t.description) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.nom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.prenom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(t.statut) LIKE LOWER(:searchTerm)",
                    TicketSupport.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public int countTicketsByStatut(String statut) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM TicketSupport t WHERE t.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            return query.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }

    // Calcule le temps moyen de résolution des tickets (en heures)
    public double calculateAverageResolutionTime() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Object[]> query = em.createQuery(
                    "SELECT t.dateOuverture, t.dateFermeture FROM TicketSupport t " +
                            "WHERE t.statut = 'Résolu' OR t.statut = 'Fermé'", Object[].class);

            List<Object[]> results = query.getResultList();
            if (results.isEmpty()) {
                return 0;
            }

            double totalHours = 0;
            for (Object[] result : results) {
                LocalDateTime openDate = (LocalDateTime) result[0];
                LocalDateTime closeDate = (LocalDateTime) result[1];

                if (closeDate != null) {
                    // Calculer le nombre d'heures entre l'ouverture et la fermeture
                    double hours = java.time.Duration.between(openDate, closeDate).toHours();
                    totalHours += hours;
                }
            }

            return totalHours / results.size();
        } finally {
            em.close();
        }
    }

    public TicketSupport save(TicketSupport ticket) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (ticket.getId() == null) {
                em.persist(ticket);
            } else {
                ticket = em.merge(ticket);
            }
            em.getTransaction().commit();
            return ticket;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(TicketSupport ticket) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(ticket)) {
                ticket = em.merge(ticket);
            }
            em.remove(ticket);
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
            TicketSupport ticket = em.find(TicketSupport.class, id);
            if (ticket != null) {
                em.remove(ticket);
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