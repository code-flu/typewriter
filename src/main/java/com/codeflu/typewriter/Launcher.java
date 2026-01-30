package com.codeflu.typewriter;

import javafx.application.Application;

/**
 * Launcher class for the Typewriter application.
 * This class serves as the entry point to launch the JavaFX application,
 * allowing proper module initialization.
 */
public class Launcher {

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
