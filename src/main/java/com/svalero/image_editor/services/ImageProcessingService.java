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

                for (ImageFilter filter : filters) {
                    image = filter.apply(image);
                }

                return image;
            }
        };
    }

    // PROCESA IMAGENE
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
                    processedImages.add(processedImage);
                    System.out.println("Processed image added: " + file.getName());
                } else {
                    System.out.println("Processed image is null for: " + file.getName());
                }
                latch.countDown();
            });

            service.setOnFailed(event -> {
                System.err.println("Error processing image: " + service.getException().getMessage());
                latch.countDown();
            });

            service.start();
        }

        latch.await();
        System.out.println("Total processed images: " + processedImages.size());
        return processedImages;
    }
}