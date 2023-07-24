module com.codeflu.typewriter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.desktop;

    opens com.codeflu.typewriter to javafx.fxml;
    exports com.codeflu.typewriter;
}