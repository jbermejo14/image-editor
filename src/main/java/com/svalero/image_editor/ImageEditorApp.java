package com.svalero.image_editor;

import com.svalero.image_editor.controllers.MainController;
import com.svalero.image_editor.utils.SplashScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImageEditorApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create and show the splash screen
        SplashScreen splashScreen = new SplashScreen();
        Stage splashStage = new Stage();
        splashScreen.start(splashStage);

        // Load the main application in a separate thread
        new Thread(() -> {
            try {
                // Simulate loading time (you can replace this with actual loading logic)
                Thread.sleep(3000); // Simulate a loading delay

                // Load the main application
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/svalero/image_editor/MainView.fxml"));
                Scene scene = new Scene(loader.load());

                // Get the controller instance and initialize it
                MainController controller = loader.getController();
                controller.initialize(); // Call the initialization method

                // Update the JavaFX Application Thread to show the main application
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setTitle("Image Editor");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    splashStage.close(); // Close the splash screen
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start(); // Start the loading thread
    }

    public static void main(String[] args) {
        launch(args);
    }
}