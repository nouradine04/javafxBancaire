package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class RemboursementRepository {

    public List<Remboursement> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Remboursement> query = em.createQuery("SELECT r FROM Remboursement r ORDER BY r.date DESC", Remboursement.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Remboursement> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Remboursement remboursement = em.find(Remboursement.class, id);
            return Optional.ofNullable(remboursement);
        } finally {
            em.close();
        }
    }

    public List<Remboursement> findByCreditId(Long creditId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Remboursement> query = em.createQuery(
                    "SELECT r FROM Remboursement r WHERE r.credit.id = :creditId ORDER BY r.date DESC", Remboursement.class);
            query.setParameter("creditId", creditId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Remboursement save(Remboursement remboursement) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (remboursement.getId() == null) {
                em.persist(remboursement);
            } else {
                remboursement = em.merge(remboursement);
            }
            em.getTransaction().commit();
            return remboursement;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Remboursement remboursement) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(remboursement)) {
                remboursement = em.merge(remboursement);
            }
            em.remove(remboursement);
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
            Remboursement remboursement = em.find(Remboursement.class, id);
            if (remboursement != null) {
                em.remove(remboursement);
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