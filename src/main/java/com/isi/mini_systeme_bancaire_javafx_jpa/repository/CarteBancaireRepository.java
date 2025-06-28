package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.CarteBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CarteBancaireRepository {

    public List<CarteBancaire> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c " +
                            "LEFT JOIN FETCH c.compte cp " +
                            "LEFT JOIN FETCH cp.client",
                    CarteBancaire.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<CarteBancaire> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c " +
                            "LEFT JOIN FETCH c.compte cp " +
                            "LEFT JOIN FETCH cp.client " +
                            "WHERE c.id = :id",
                    CarteBancaire.class
            );
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<CarteBancaire> findByNumero(String numero) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c " +
                            "LEFT JOIN FETCH c.compte cp " +
                            "LEFT JOIN FETCH cp.client " +
                            "WHERE c.numero = :numero",
                    CarteBancaire.class
            );
            query.setParameter("numero", numero);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }

    public List<CarteBancaire> findByCompteId(Long compteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c " +
                            "LEFT JOIN FETCH c.compte cp " +
                            "LEFT JOIN FETCH cp.client " +
                            "WHERE c.compte.id = :compteId",
                    CarteBancaire.class
            );
            query.setParameter("compteId", compteId);
            return query.getResultList();
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }

    public List<CarteBancaire> findCartesExpirees() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c " +
                            "WHERE c.dateExpiration < :currentDate",
                    CarteBancaire.class
            );
            query.setParameter("currentDate", LocalDate.now());
            return query.getResultList();
        } catch (Exception e) {
            throw e;
        } finally {
            em.close();
        }
    }

    public CarteBancaire save(CarteBancaire carteBancaire) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Validation des données avant sauvegarde
            if (carteBancaire.getNumero() == null || carteBancaire.getNumero().isEmpty()) {
                throw new IllegalArgumentException("Le numéro de carte ne peut pas être vide");
            }

            if (carteBancaire.getId() == null) {
                em.persist(carteBancaire);
            } else {
                carteBancaire = em.merge(carteBancaire);
            }

            em.getTransaction().commit();
            return carteBancaire;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(CarteBancaire carteBancaire) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(carteBancaire)) {
                carteBancaire = em.merge(carteBancaire);
            }
            em.remove(carteBancaire);
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
            CarteBancaire carteBancaire = em.find(CarteBancaire.class, id);
            if (carteBancaire != null) {
                em.remove(carteBancaire);
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