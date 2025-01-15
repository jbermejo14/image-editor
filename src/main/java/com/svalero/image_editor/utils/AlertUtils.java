package com.svalero.image_editor.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtils {

    public static void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
