package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class CreditRepository {

    public List<Credit> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Credit> query = em.createQuery("SELECT c FROM Credit c ORDER BY c.dateDemande DESC", Credit.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Credit> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Credit credit = em.find(Credit.class, id);
            return Optional.ofNullable(credit);
        } finally {
            em.close();
        }
    }

    public List<Credit> findByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Credit> query = em.createQuery(
                    "SELECT c FROM Credit c WHERE c.client.id = :clientId ORDER BY c.dateDemande DESC", Credit.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Credit> searchCredits(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Credit> query = em.createQuery(
                    "SELECT c FROM Credit c WHERE " +
                            "CAST(c.montant AS STRING) LIKE :searchTerm OR " +
                            "c.statut LIKE :searchTerm OR " +
                            "c.client.nom LIKE :searchTerm OR " +
                            "c.client.prenom LIKE :searchTerm", Credit.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Credit save(Credit credit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (credit.getId() == null) {
                em.persist(credit);
            } else {
                credit = em.merge(credit);
            }
            em.getTransaction().commit();
            return credit;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Credit credit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(credit)) {
                credit = em.merge(credit);
            }
            em.remove(credit);
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
            Credit credit = em.find(Credit.class, id);
            if (credit != null) {
                em.remove(credit);
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