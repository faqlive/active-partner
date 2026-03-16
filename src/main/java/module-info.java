module org.example.activepartner {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires com.google.gson;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.github.kwhat.jnativehook;
    requires java.logging;

    opens org.example.activepartner to javafx.fxml;
    opens org.example.activepartner.ui to javafx.fxml;
    opens org.example.activepartner.ui.components to javafx.fxml;
    opens org.example.activepartner.domain to com.google.gson;
    exports org.example.activepartner;
}
