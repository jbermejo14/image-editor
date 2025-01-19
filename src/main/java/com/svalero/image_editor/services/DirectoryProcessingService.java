package com.svalero.image_editor.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import com.svalero.image_editor.filters.ImageFilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DirectoryProcessingService extends Service<List<BufferedImage>> {
    private List<File> imageFiles;
    private List<ImageFilter> filters;

    public void setImageFiles(List<File> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public void setFilters(List<ImageFilter> filters) {
        this.filters = filters;
    }

    @Override
    protected Task<List<BufferedImage>> createTask() {
        return new Task<>() {
            @Override
            protected List<BufferedImage> call() throws Exception {
                List<BufferedImage> processedImages = new ArrayList<>();
                CountDownLatch latch = new CountDownLatch(imageFiles.size());

                for (File file : imageFiles) {
                    ImageProcessingService service = new ImageProcessingService();
                    service.setImageFile(file);
                    service.setFilters(filters);

                    service.setOnSucceeded(event -> {
                        processedImages.add(service.getValue()); // Get the processed image
                        latch.countDown(); // Decrement the latch count
                    });

                    service.setOnFailed(event -> {
                        System.err.println("Error processing image: " + service.getException().getMessage());
                        latch.countDown(); // Decrement the latch count even on failure
                    });

                    service.start(); // Start the service
                }

                latch.await(); // Wait for all services to complete
                return processedImages; // Return the list of processed images
            }
        };
    }
}