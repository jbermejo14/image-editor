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
        SplashScreen splashScreen = new SplashScreen();
        Stage splashStage = new Stage();
        splashScreen.start(splashStage);

        new Thread(() -> {
            try {
                Thread.sleep(3000); // SIMULACION DE RETARDO

                // CARGA LA APPLICACION PRINCIPAL
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/svalero/image_editor/MainView.fxml"));
                Scene scene = new Scene(loader.load());

                MainController controller = loader.getController();
                controller.initialize();

                javafx.application.Platform.runLater(() -> {
                    primaryStage.setTitle("Image Editor");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    splashStage.close();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}