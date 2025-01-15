package com.svalero.image_editor.controllers;

import com.svalero.image_editor.services.ImageProcessingService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.svalero.image_editor.filters.*;
import com.svalero.image_editor.history.ImageHistory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController extends Application {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView processedImageView;
    @FXML
    private TextArea historyArea;
    @FXML
    private ProgressBar progressBar;

    private List<ImageFilter> filters;
    private ImageHistory history;
    private BufferedImage processedImage;
    private ImageProcessingService imageProcessingService;

    public MainController() {
        filters = new ArrayList<>();
        history = new ImageHistory();
        imageProcessingService = new ImageProcessingService();
    }

    @Override
    public void start(Stage primaryStage) {
        showSplashScreen(primaryStage);
    }

    private void showSplashScreen(Stage primaryStage) {
        Alert splash = new Alert(Alert.AlertType.INFORMATION);
        splash.setTitle("Cargando");
        splash.setHeaderText(null);
        splash.setContentText("Cargando la aplicacion...");
        splash.show();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                splash.close();
                primaryStage.show();
            });
        }).start();
    }

    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            processImage(file);
        }
    }


    @FXML
    private void saveImages() {
        BufferedImage processedImage = SwingFXUtils.fromFXImage(processedImageView.getImage(), null);

        if (processedImage == null) {
            showErrorPopup("No hay imagen seleccionada para guardar.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona el directorio");
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            String originalFileName = "processed_image.png";
            saveProcessedImage(processedImage, originalFileName, selectedDirectory.getAbsolutePath());
            showCompletionPopup("Imagen guardada en: " + selectedDirectory.getAbsolutePath());
        } else {
            showErrorPopup("Directorio no selecciondo.");
        }
    }

    private void processImage(File file) {
        List<ImageFilter> filters = new ArrayList<>();
        imageProcessingService.setImageFile(file);
        imageProcessingService.setFilters(filters);

        progressBar.progressProperty().bind(imageProcessingService.progressProperty());

        imageProcessingService.setOnSucceeded(event -> {
            BufferedImage processedImage = imageProcessingService.getValue();
            Image fxImage = SwingFXUtils.toFXImage(processedImage, null);
            Platform.runLater(() -> originalImageView.setImage(fxImage));
        });

        imageProcessingService.setOnFailed(event -> {
            Throwable exception = imageProcessingService.getException();
            System.err.println("Error procesando la imagen: " + exception.getMessage());
            exception.printStackTrace();
        });

        imageProcessingService.restart();
    }

    private void saveProcessedImage(BufferedImage image, String originalFileName, String directoryPath) {
        File saveFile = new File(directoryPath, "processed_" + originalFileName);
        try {
            ImageIO.write(image, "png", saveFile);
            history.addEntry("Imagen procesada guardada: " + saveFile.getName());
            updateHistory();
        } catch (IOException e) {
            showErrorPopup ("Error guardando imagen: " + saveFile.getName());
        }
    }

    private void showCompletionPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Finalización");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveImage() {
        BufferedImage processedImage = SwingFXUtils.fromFXImage(processedImageView.getImage(), null);

        if (processedImage == null) {
            showErrorPopup("No hay imagen procesada para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Imagen procesada guardada");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        String defaultFileName = "processed_image.png";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                ImageIO.write(processedImage, "png", file);
                history.addEntry("Imagen procesada guardada: " + file.getName());
                updateHistory();
                showCompletionPopup("Imagen guardada como: " + file.getName());
            } catch (IOException e) {
                showErrorPopup("Error guardando imagen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void applyFilters() {
        BufferedImage originalImage = SwingFXUtils.fromFXImage(originalImageView.getImage(), null);
        if (originalImage == null || filters.isEmpty()) {
            return;
        }

        BufferedImage processedImage = originalImage;
        for (ImageFilter filter : filters) {
            processedImage = filter.apply(processedImage);
        }

        processedImageView.setImage(SwingFXUtils.toFXImage(processedImage, null));
        history.addEntry("Applied filters: " + filters.toString());
        updateHistory();
    }

    @FXML
    private void addGrayscaleFilter() {
        filters.add(new GrayscaleFilter());
        history.addEntry("Filtro Grayscale añadido");
        updateHistory();
    }

    @FXML
    private void addInvertFilter() {
        filters.add(new InvertColorFilter());
        history.addEntry("Filtro Inversion añadido");
        updateHistory();
    }

    @FXML
    private void addBrightnessFilter() {
        filters.add(new BrightnessFilter(30));
        history.addEntry("Filtro brillo añadido");
        updateHistory();
    }

    private void updateHistory() {
        historyArea.setText(String.join("\n", history.getEntries()));
    }
}