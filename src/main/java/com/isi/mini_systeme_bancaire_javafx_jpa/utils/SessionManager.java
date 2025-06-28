package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
public class SessionManager {
    // Instances des utilisateurs actuellement connectés
    private static Admin currentAdmin;
    private static Client currentClient;
    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }
    public static void setCurrentClient(Client client) {
        currentClient = client;
    }
    public static Client getCurrentClient() {
        return currentClient;
    }
    public static boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }
    public static boolean isClientLoggedIn() {
        return currentClient != null;
    }
    public static void logout() {
        currentAdmin = null;
        currentClient = null;
    }
}