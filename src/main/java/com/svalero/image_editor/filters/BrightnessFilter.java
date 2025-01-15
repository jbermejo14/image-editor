package com.svalero.image_editor.filters;

import java.awt.image.BufferedImage;

public class BrightnessFilter implements ImageFilter {
    private int brightness;

    public BrightnessFilter(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage brightenedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xff) + brightness));
                int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xff) + brightness));
                int b = Math.min(255, Math.max(0, (rgb & 0xff) + brightness));
                int newRgb = (r << 16) | (g << 8) | b;
                brightenedImage.setRGB(x, y, newRgb);
            }
        }
        return brightenedImage;
    }
}