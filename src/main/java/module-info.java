module com.example.ccc151_proj {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.mail;
    requires activation;

    opens com.example.ccc151_proj to javafx.fxml;
    exports com.example.ccc151_proj;

    opens com.example.ccc151_proj.control to javafx.fxml;
    exports com.example.ccc151_proj.control;

    opens com.example.ccc151_proj.model to javafx.fxml;
    exports com.example.ccc151_proj.model;

}