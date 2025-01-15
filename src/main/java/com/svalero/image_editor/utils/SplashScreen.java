package com.svalero.image_editor;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SplashScreen extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage splashStage = new Stage();
        Label splashLabel = new Label("Cargando Image Editor...");
        StackPane splashPane = new StackPane(splashLabel);
        Scene splashScene = new Scene(splashPane, 400, 200);

        splashStage.setScene(splashScene);
        splashStage.setTitle("Cargando...");
        splashStage.show();

        Task<Void> splashTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000);
                return null;
            }
        };

        splashTask.setOnSucceeded(event -> {
            splashStage.close();
            try {
                showMainApplication(primaryStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        new Thread(splashTask).start();
    }

    private void showMainApplication(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene mainScene = new Scene(loader.load());
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Image Editor");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
