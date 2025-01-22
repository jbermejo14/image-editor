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
        progressIndicator.setProgress(-1);

        StackPane splashPane = new StackPane(splashLabel, progressIndicator);
        Scene splashScene = new Scene(splashPane, 400, 200);

        splashStage.setScene(splashScene);
        splashStage.setTitle("Cargando...");
        splashStage.show();

        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000); // CUIDADO AL QUITARLO, FALLA ALGUNA VEZ
                return null;
            }
        };

        loadingTask.setOnSucceeded(event -> {
            try {
                showMainApplication(splashStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        new Thread(loadingTask).start();
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