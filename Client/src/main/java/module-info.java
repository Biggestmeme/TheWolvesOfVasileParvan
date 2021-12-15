module com.bursierii.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires kafka.clients;

    opens com.bursierii.client to javafx.fxml;
    exports com.bursierii.client;
//    exports com.bursierii.client.controllers;
//    opens com.bursierii.client.controllers to javafx.fxml;
}