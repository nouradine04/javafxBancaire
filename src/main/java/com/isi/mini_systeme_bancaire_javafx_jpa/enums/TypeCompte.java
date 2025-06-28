package com.isi.mini_systeme_bancaire_javafx_jpa.enums;


public enum TypeCompte {
    COURANT("Courant"),
    EPARGNE("Épargne");

    private final String libelle;

    TypeCompte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}