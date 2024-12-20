module org.example.gameoflife {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.gameoflife to javafx.fxml;
    exports org.example.gameoflife;
}