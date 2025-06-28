module com.isi.mini_systeme_bancaire_javafx_jpa {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires TrayNotification;

    // JPA et Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Other Dependencies
    requires kernel;
    requires layout;
    requires io;
    requires java.mail;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;
    requires transitive java.desktop;
    requires org.apache.logging.log4j;

    opens com.isi.mini_systeme_bancaire_javafx_jpa to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.controller.client to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.controller.client to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.controller to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.controller to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.enums to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.enums to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.mapper to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.mapper to javafx.fxml;

    // Open model package to both JavaFX and Hibernate
    opens com.isi.mini_systeme_bancaire_javafx_jpa.model to javafx.fxml, org.hibernate.orm.core, javafx.base;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.model to javafx.fxml, javafx.base;

    opens com.isi.mini_systeme_bancaire_javafx_jpa.repository to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.repository to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.request to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.request to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.response to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.response to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.utils to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.utils to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.service.impl to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.service.impl to javafx.fxml;
    opens com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces to javafx.fxml;
    exports com.isi.mini_systeme_bancaire_javafx_jpa;
}