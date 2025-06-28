package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CompteRepository {

    public List<Compte> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery("SELECT c FROM Compte c ORDER BY c.dateCreation DESC", Compte.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> findAllWithClients() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT DISTINCT c FROM Compte c LEFT JOIN FETCH c.client ORDER BY c.dateCreation DESC",
                    Compte.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Compte> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Compte compte = em.find(Compte.class, id);
            return Optional.ofNullable(compte);
        } finally {
            em.close();
        }
    }

    public Optional<Compte> findByIdWithClient(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.id = :id",
                    Compte.class
            );
            query.setParameter("id", id);
            List<Compte> comptes = query.getResultList();
            return comptes.isEmpty() ? Optional.empty() : Optional.of(comptes.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Compte> findByNumero(String numero) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c WHERE c.numero = :numero",
                    Compte.class
            );
            query.setParameter("numero", numero);
            List<Compte> comptes = query.getResultList();
            return comptes.isEmpty() ? Optional.empty() : Optional.of(comptes.get(0));
        } finally {
            em.close();
        }
    }

    public List<Compte> findByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c WHERE c.client.id = :clientId",
                    Compte.class
            );
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> searchComptes(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c WHERE " +
                            "c.numero LIKE :searchTerm OR " +
                            "c.type LIKE :searchTerm OR " +
                            "c.client.nom LIKE :searchTerm OR " +
                            "c.client.prenom LIKE :searchTerm",
                    Compte.class
            );
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> searchComptesWithClients(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT DISTINCT c FROM Compte c LEFT JOIN FETCH c.client WHERE " +
                            "c.numero LIKE :searchTerm OR " +
                            "c.type LIKE :searchTerm OR " +
                            "c.client.nom LIKE :searchTerm OR " +
                            "c.client.prenom LIKE :searchTerm",
                    Compte.class
            );
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Compte save(Compte compte) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (compte.getId() == null) {
                em.persist(compte);
            } else {
                compte = em.merge(compte);
            }
            em.getTransaction().commit();
            return compte;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Compte compte) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(compte)) {
                compte = em.merge(compte);
            }
            em.remove(compte);
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
            Compte compte = em.find(Compte.class, id);
            if (compte != null) {
                em.remove(compte);
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