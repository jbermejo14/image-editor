package com.svalero.image_editor.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;
import com.svalero.image_editor.filters.ImageFilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ImageProcessingService extends Service<BufferedImage> {
    private File imageFile;
    private List<ImageFilter> filters;

    public void setImageFile(File file) {
        this.imageFile = file;
    }

    public void setFilters(List<ImageFilter> filters) {
        this.filters = filters;
    }

    @Override
    protected Task<BufferedImage> createTask() {
        return new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                if (imageFile == null) {
                    throw new IllegalStateException("No hay archivo seleccionado.");
                }

                BufferedImage image = ImageIO.read(imageFile);
                System.out.println("Original image size: " + image.getWidth() + "x" + image.getHeight());

                for (ImageFilter filter : filters) {
                    image = filter.apply(image);
                    System.out.println("Applied filter: " + filter.getClass().getSimpleName());
                    if (image == null) {
                        System.out.println("Image became null after applying filter: " + filter.getClass().getSimpleName());
                    } else {
                        System.out.println("Processed image size: " + image.getWidth() + "x" + image.getHeight());
                    }
                }

                return image; // Return the processed image
            }
        };
    }

    // Method to process multiple images
    public static List<BufferedImage> processImages(List<File> imageFiles, List<ImageFilter> filters) throws InterruptedException {
        List<BufferedImage> processedImages = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(imageFiles.size());

        for (File file : imageFiles) {
            ImageProcessingService service = new ImageProcessingService();
            service.setImageFile(file);
            service.setFilters(filters);

            service.setOnSucceeded(event -> {
                BufferedImage processedImage = service.getValue();
                if (processedImage != null) {
                    processedImages.add(processedImage); // Get the processed image
                    System.out.println("Processed image added: " + file.getName());
                } else {
                    System.out.println("Processed image is null for: " + file.getName());
                }
                latch.countDown(); // Decrement the latch count
            });

            service.setOnFailed(event -> {
                System.err.println("Error processing image: " + service.getException().getMessage());
                latch.countDown(); // Decrement the latch count even on failure
            });

            service.start(); // Start the service
        }

        latch.await(); // Wait for all services to complete
        System.out.println("Total processed images: " + processedImages.size());
        return processedImages; // Return the list of processed images
    }
}