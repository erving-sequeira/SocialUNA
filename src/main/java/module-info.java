module cr.ac.una.est.socialuna.socialuna {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports cr.ac.una.est.socialuna.app;
    exports cr.ac.una.est.socialuna.model.entity;

    // Gson necesita reflejar los campos privados de los documentos y las entidades.
    opens cr.ac.una.est.socialuna.persistence to com.google.gson;
    opens cr.ac.una.est.socialuna.model.entity to com.google.gson;

    // FXMLLoader necesita reflejar los controladores del paquete UI.
    opens cr.ac.una.est.socialuna.ui.controller to javafx.fxml;
}
