package com.svalero.image_editor.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import com.svalero.image_editor.filters.ImageFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

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
                    throw new IllegalStateException("No hay archivo seleccinado.");
                }

                BufferedImage image = ImageIO.read(imageFile);

                for (ImageFilter filter : filters) {
                    image = filter.apply(image);
                }

                return image;
            }
        };
    }
}
