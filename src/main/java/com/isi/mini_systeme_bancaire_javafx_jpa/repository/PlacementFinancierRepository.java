//package com.isi.mini_systeme_bancaire_javafx_jpa.repository;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.PlacementFinancier;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.TypedQuery;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//public class PlacementFinancierRepository {
//
//    public List<PlacementFinancier> findAll() {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p ORDER BY p.dateDebut DESC", PlacementFinancier.class);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public Optional<PlacementFinancier> findById(Long id) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            PlacementFinancier placement = em.find(PlacementFinancier.class, id);
//            return Optional.ofNullable(placement);
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<PlacementFinancier> findByClientId(Long clientId) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p WHERE p.client.id = :clientId ORDER BY p.dateDebut DESC",
//                    PlacementFinancier.class);
//            query.setParameter("clientId", clientId);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<PlacementFinancier> findByCompteId(Long compteId) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p WHERE p.compte.id = :compteId ORDER BY p.dateDebut DESC",
//                    PlacementFinancier.class);
//            query.setParameter("compteId", compteId);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<PlacementFinancier> findByStatut(String statut) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p WHERE p.statut = :statut ORDER BY p.dateDebut DESC",
//                    PlacementFinancier.class);
//            query.setParameter("statut", statut);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<PlacementFinancier> findByType(String type) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p WHERE p.type = :type ORDER BY p.dateDebut DESC",
//                    PlacementFinancier.class);
//            query.setParameter("type", type);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<PlacementFinancier> findPlacementsActifsExpires() {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            // Trouver les placements actifs dont la date d'échéance est dépassée
//            TypedQuery<PlacementFinancier> query = em.createQuery(
//                    "SELECT p FROM PlacementFinancier p WHERE p.statut = 'Actif' AND p.dateEcheance < :today",
//                    PlacementFinancier.class);
//            query.setParameter("today", LocalDate.now());
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public double getSommePlacementsByClientId(Long clientId) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            TypedQuery<Double> query = em.createQuery(
//                    "SELECT SUM(p.montant) FROM PlacementFinancier p WHERE p.client.id = :clientId AND p.statut = 'Actif'",
//                    Double.class);
//            query.setParameter("clientId", clientId);
//            Double result = query.getSingleResult();
//            return result != null ? result : 0.0;
//        } finally {
//            em.close();
//        }
//    }
//
//    public PlacementFinancier save(PlacementFinancier placement) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            em.getTransaction().begin();
//            if (placement.getId() == null) {
//                em.persist(placement);
//            } else {
//                placement = em.merge(placement);
//            }
//            em.getTransaction().commit();
//            return placement;
//        } catch (Exception e) {
//            if (em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//            throw e;
//        } finally {
//            em.close();
//        }
//    }
//
//    public void delete(PlacementFinancier placement) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            em.getTransaction().begin();
//            if (!em.contains(placement)) {
//                placement = em.merge(placement);
//            }
//            em.remove(placement);
//            em.getTransaction().commit();
//        } catch (Exception e) {
//            if (em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//            throw e;
//        } finally {
//            em.close();
//        }
//    }
//
//    public void deleteById(Long id) {
//        EntityManager em = JpaUtil.getEntityManager();
//        try {
//            em.getTransaction().begin();
//            PlacementFinancier placement = em.find(PlacementFinancier.class, id);
//            if (placement != null) {
//                em.remove(placement);
//            }
//            em.getTransaction().commit();
//        } catch (Exception e) {
//            if (em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//            throw e;
//        } finally {
//            em.close();
//        }
//    }
//}