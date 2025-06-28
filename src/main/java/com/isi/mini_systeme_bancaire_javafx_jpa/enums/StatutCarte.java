package com.isi.mini_systeme_bancaire_javafx_jpa.enums;

public enum StatutCarte {
    ACTIVE("Active"),
    BLOQUEE("Bloquée");

    private final String libelle;

    StatutCarte(String libelle) {
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