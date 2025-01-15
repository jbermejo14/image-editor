package com.svalero.image_editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.svalero.image_editor.controllers.MainController;

public class ImageEditorApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/svalero/image_editor/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Image Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
