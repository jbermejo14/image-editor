package com.svalero.image_editor.utils;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SplashScreen extends Application {

    @Override
    public void start(Stage splashStage) {
        Label splashLabel = new Label("Cargando Image Editor...");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Indeterminate progress

        StackPane splashPane = new StackPane(splashLabel, progressIndicator);
        Scene splashScene = new Scene(splashPane, 400, 200);

        splashStage.setScene(splashScene);
        splashStage.setTitle("Cargando...");
        splashStage.show();

        // Simulate loading in a background task
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate loading time (e.g., loading resources, initializing components)
                Thread.sleep(3000); // Simulate a loading delay
                return null;
            }
        };

        loadingTask.setOnSucceeded(event -> {
            try {
                showMainApplication(splashStage); // Show the main application
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        new Thread(loadingTask).start(); // Start the loading task in a new thread
    }

    private void showMainApplication(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/svalero/image_editor/MainView.fxml"));
        Scene mainScene = new Scene(loader.load());
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Image Editor");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}