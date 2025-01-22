package com.svalero.image_editor.controllers;

import com.svalero.image_editor.services.DirectoryProcessingService;
import com.svalero.image_editor.services.ImageProcessingService;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import com.svalero.image_editor.filters.*;
import com.svalero.image_editor.history.ImageHistory;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView processedImageView;
    @FXML
    private TextArea historyArea;
    @FXML
    private ProgressBar progressBar;

    private final List<ImageFilter> filters;
    private final ImageHistory history;
    private final ImageProcessingService imageProcessingService;
    private final List<File> imageFiles = new ArrayList<>();

    public MainController() {
        filters = new ArrayList<>();
        history = new ImageHistory();
        imageProcessingService = new ImageProcessingService();
    }

    public void initialize() {
        System.out.println("MainController Iniciado.");
    }

//  ABRE 1 IMAGEN
    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            processImage(file);
        }
    }

//  ABRE UNA CARPETA CON IMAGENES
    @FXML
    private void openImagesFromDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

            if (files != null && files.length > 0) {
                imageFiles.clear();
                imageFiles.addAll(Arrays.asList(files));

                DirectoryProcessingService directoryProcessingService = new DirectoryProcessingService();
                directoryProcessingService.setImageFiles(imageFiles);
                directoryProcessingService.setFilters(filters);

                // BARRA DE PROGRESO
                progressBar.setVisible(true);
                progressBar.progressProperty().unbind();
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

                // UNE LA BARRA DE PROGRESO AL PROCESO
                progressBar.progressProperty().bind(directoryProcessingService.progressProperty());

                directoryProcessingService.setOnSucceeded(event -> {
                    List<BufferedImage> processedImages = directoryProcessingService.getValue();
                    System.out.println("Processed images count: " + processedImages.size());

                    if (processedImages.isEmpty()) {
                        System.out.println("No images selected after processing.");
                        progressBar.setVisible(false);
                        return;
                    }

                    Platform.runLater(() -> {
                        for (BufferedImage processedImage : processedImages) {
                            Image fxImage = SwingFXUtils.toFXImage(processedImage, null);
                            originalImageView.setImage(fxImage);
                        }
                        progressBar.setVisible(false);
                    });
                });

                directoryProcessingService.setOnFailed(event -> {
                    showErrorPopup("Error processing images: " + directoryProcessingService.getException().getMessage());
                    progressBar.setVisible(false);
                });
                directoryProcessingService.start();
            } else {
                showErrorPopup("No hay imagenes en el directorio seleccionado.");
            }
        } else {
            showErrorPopup("No hay directorio seleccionado");
        }
    }

    @FXML
    private void saveImages() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona el directorio para guardar las imágenes procesadas");
        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null) {
            if (imageFiles == null || imageFiles.isEmpty()) {
                showErrorPopup("No hay imágenes para procesar.");
                return;
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    List<BufferedImage> processedImages = ImageProcessingService.processImages(imageFiles, filters);

                    if (processedImages.isEmpty()) {
                        Platform.runLater(() -> showErrorPopup("No hay imágenes procesadas para guardar."));
                        return;
                    }

                    CountDownLatch latch = new CountDownLatch(processedImages.size());
                    for (int i = 0; i < processedImages.size(); i++) {
                        BufferedImage image = processedImages.get(i);
                        String fileName = "processed_image_" + (i + 1) + ".png";

                        try {
                            saveProcessedImage(image, fileName, selectedDirectory.getAbsolutePath());
                        } catch (Exception e) {
                            System.err.println("Error saving image " + fileName + ": " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }

                    latch.await();
                    Platform.runLater(() -> showCompletionPopup("Todas las imágenes han sido guardadas en: " + selectedDirectory.getAbsolutePath()));
                } catch (Exception e) {
                    Platform.runLater(() -> showErrorPopup("Error durante el procesamiento de las imágenes: " + e.getMessage()));
                } finally {
                    executor.shutdown();
                }
            });
        } else {
            showErrorPopup("Directorio no seleccionado.");
        }
    }

    private void processImage(File file) {
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
            showErrorPopup("Error guardando imagen: " + saveFile.getName());
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