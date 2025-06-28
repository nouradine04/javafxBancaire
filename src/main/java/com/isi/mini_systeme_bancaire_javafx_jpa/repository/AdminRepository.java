package com.isi.mini_systeme_bancaire_javafx_jpa.repository;


import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class AdminRepository {

    public List<Admin> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Admin> query = em.createQuery("SELECT a FROM Admin a", Admin.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Admin> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Admin admin = em.find(Admin.class, id);
            return Optional.ofNullable(admin);
        } finally {
            em.close();
        }
    }

    public Optional<Admin> findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Admin> query = em.createQuery(
                    "SELECT a FROM Admin a WHERE a.username = :username", Admin.class);
            query.setParameter("username", username);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public boolean authenticate(String username, String password) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Admin> query = em.createQuery(
                    "SELECT a FROM Admin a WHERE a.username = :username AND a.password = :password", Admin.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return !query.getResultList().isEmpty();
        } finally {
            em.close();
        }
    }

    public Admin save(Admin admin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (admin.getId() == null) {
                em.persist(admin);
            } else {
                admin = em.merge(admin);
            }
            em.getTransaction().commit();
            return admin;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Admin admin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(admin)) {
                admin = em.merge(admin);
            }
            em.remove(admin);
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
            Admin admin = em.find(Admin.class, id);
            if (admin != null) {
                em.remove(admin);
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