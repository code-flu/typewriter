package com.codeflu.typewriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point for the Typewriter application.
 * This class initializes the JavaFX application and loads the main UI.
 */
public class App extends Application {

    private static final String WINDOW_TITLE = "Typewriter";
    private static final String FXML_FILE = "typewriter.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML());
        stage.setScene(scene);
        stage.setTitle(WINDOW_TITLE);
        stage.show();
    }

    /**
     * Loads the FXML file for the main UI.
     *
     * @return the root parent node of the loaded FXML
     * @throws IOException if the FXML file cannot be loaded
     */
    private static Parent loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML_FILE));
        return fxmlLoader.load();
    }

}
