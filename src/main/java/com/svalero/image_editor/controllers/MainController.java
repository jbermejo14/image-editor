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

    private List<ImageFilter> filters = new ArrayList<>(); // Assuming you have filters defined
    private ImageHistory history;
    private ImageProcessingService imageProcessingService;
    private List<File> imageFiles = new ArrayList<>(); // Class variable to hold image files

    public MainController() {
        filters = new ArrayList<>();
        history = new ImageHistory();
        imageProcessingService = new ImageProcessingService();
    }

    // This method will be called after the main application is loaded
    public void initialize() {
        // Perform any necessary setup here
        System.out.println("MainController initialized.");
        // You can load default images or set up services here if needed
    }

    private void saveImagesInBackground(List<BufferedImage> images, File directory) {
        Service<Void> saveService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        for (int i = 0; i < images.size(); i++) {
                            BufferedImage image = images.get(i);
                            File outputFile = new File(directory, "processed_image_" + (i + 1) + ".png");
                            ImageIO.write(image, "png", outputFile);
                        }
                        return null;
                    }
                };
            }
        };

        saveService.setOnSucceeded(event -> {
            // Update UI after saving is complete
            showCompletionPopup("Images saved successfully!");
            progressBar.setVisible(false);
        });

        saveService.setOnFailed(event -> {
            // Handle any errors that occurred during saving
            showErrorPopup("Error saving images: " + saveService.getException().getMessage());
            progressBar.setVisible(false);
        });

        // Show progress indicator
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        // Start the service
        saveService.start();
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
    private void openImagesFromDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

            if (files != null && files.length > 0) {
                imageFiles.clear(); // Clear previous files
                imageFiles.addAll(Arrays.asList(files)); // Add new files to the class-level variable
                System.out.println("Loaded files: " + imageFiles);

                // Create and configure the DirectoryProcessingService
                DirectoryProcessingService directoryProcessingService = new DirectoryProcessingService();
                directoryProcessingService.setImageFiles(imageFiles);
                directoryProcessingService.setFilters(filters); // Assuming filters is a class variable

                // Show a progress indicator
                progressBar.setVisible(true);
                progressBar.progressProperty().unbind(); // Unbind previous bindings
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

                // Bind progress bar to the service's progress
                progressBar.progressProperty().bind(directoryProcessingService.progressProperty());

                directoryProcessingService.setOnSucceeded(event -> {
                    List<BufferedImage> processedImages = directoryProcessingService.getValue();
                    System.out.println("Processed images count: " + processedImages.size());

                    if (processedImages.isEmpty()) {
                        System.out.println("No images selected after processing.");
                        progressBar.setVisible(false);
                        return; // Exit if no images are processed
                    }

                    // Log details of processed images
                    for (int i = 0; i < processedImages.size(); i++) {
                        BufferedImage img = processedImages.get(i);
                        if (img != null) {
                            System.out.println("Processed image " + (i + 1) + " size: " + img.getWidth() + "x" + img.getHeight());
                        } else {
                            System.out.println("Processed image " + (i + 1) + " is null.");
                        }
                    }

                    // Update the UI with the processed images
                    Platform.runLater(() -> {
                        for (BufferedImage processedImage : processedImages) {
                            if (processedImage != null && processedImage.getWidth() > 0 && processedImage.getHeight() > 0) {
                                Image fxImage = SwingFXUtils.toFXImage(processedImage, null);
                                if (originalImageView != null) {
                                    originalImageView.setImage(fxImage);
                                } else {
                                    System.out.println("originalImageView is null.");
                                }
                            } else {
                                System.out.println("Processed image is null or empty.");
                            }
                        }
                        progressBar.setVisible(false); // Hide progress indicator
                    });
                });

                directoryProcessingService.setOnFailed(event -> {
                    showErrorPopup("Error processing images: " + directoryProcessingService.getException().getMessage());
                    progressBar.setVisible(false); // Hide progress indicator
                });

                // Start the service
                directoryProcessingService.start();
            } else {
                System.out.println("No image files found in the selected directory.");
                showErrorPopup("No image files found in the selected directory.");
            }
        } else {
            System.out.println("No directory selected.");
            showErrorPopup("No directory selected.");
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

            // Run saving task on a separate thread
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

                    latch.await(); // Wait for all images to be saved
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