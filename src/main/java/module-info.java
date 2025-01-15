module com.example.aa1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.swing;

    opens com.svalero.image_editor.controllers to javafx.fxml;
        exports com.svalero.image_editor;
        exports com.svalero.image_editor.controllers to javafx.fxml;

}