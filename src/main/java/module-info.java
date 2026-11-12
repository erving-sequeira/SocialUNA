module cr.ac.una.est.socialuna.socialuna {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports cr.ac.una.est.socialuna.app;
    exports cr.ac.una.est.socialuna.logic.model.entity;

    // Gson necesita reflejar los campos privados de las entidades.
    opens cr.ac.una.est.socialuna.persistence to com.google.gson;
    opens cr.ac.una.est.socialuna.logic.model.entity to com.google.gson;
    opens cr.ac.una.est.socialuna.logic.repository to com.google.gson;
}
