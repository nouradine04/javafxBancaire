package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.FraisBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FraisBancaireRepository {

    public List<FraisBancaire> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<FraisBancaire> query = em.createQuery(
                    "SELECT f FROM FraisBancaire f ORDER BY f.dateApplication DESC", FraisBancaire.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<FraisBancaire> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            FraisBancaire frais = em.find(FraisBancaire.class, id);
            return Optional.ofNullable(frais);
        } finally {
            em.close();
        }
    }

    public List<FraisBancaire> findByCompteId(Long compteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<FraisBancaire> query = em.createQuery(
                    "SELECT f FROM FraisBancaire f WHERE f.compte.id = :compteId ORDER BY f.dateApplication DESC",
                    FraisBancaire.class);
            query.setParameter("compteId", compteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<FraisBancaire> findByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<FraisBancaire> query = em.createQuery(
                    "SELECT f FROM FraisBancaire f WHERE f.compte.client.id = :clientId ORDER BY f.dateApplication DESC",
                    FraisBancaire.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<FraisBancaire> findByType(String type) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<FraisBancaire> query = em.createQuery(
                    "SELECT f FROM FraisBancaire f WHERE f.type = :type ORDER BY f.dateApplication DESC",
                    FraisBancaire.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<FraisBancaire> findByPeriode(LocalDate debut, LocalDate fin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<FraisBancaire> query = em.createQuery(
                    "SELECT f FROM FraisBancaire f WHERE f.dateApplication BETWEEN :debut AND :fin ORDER BY f.dateApplication ASC",
                    FraisBancaire.class);
            query.setParameter("debut", debut);
            query.setParameter("fin", fin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public double getTotalFraisByCompteId(Long compteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                    "SELECT SUM(f.montant) FROM FraisBancaire f WHERE f.compte.id = :compteId",
                    Double.class);
            query.setParameter("compteId", compteId);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    public double getTotalFraisByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                    "SELECT SUM(f.montant) FROM FraisBancaire f WHERE f.compte.client.id = :clientId",
                    Double.class);
            query.setParameter("clientId", clientId);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    public FraisBancaire save(FraisBancaire frais) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (frais.getId() == null) {
                em.persist(frais);
            } else {
                frais = em.merge(frais);
            }
            em.getTransaction().commit();
            return frais;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(FraisBancaire frais) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(frais)) {
                frais = em.merge(frais);
            }
            em.remove(frais);
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
            FraisBancaire frais = em.find(FraisBancaire.class, id);
            if (frais != null) {
                em.remove(frais);
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