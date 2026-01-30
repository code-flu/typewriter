module com.codeflu.typewriter {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing; // Required for SwingNode
    requires java.desktop;
    requires org.fife.RSyntaxTextArea;


    opens com.codeflu.typewriter to javafx.fxml;
    exports com.codeflu.typewriter;
}