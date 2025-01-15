package com.svalero.image_editor.filters;

import java.awt.image.BufferedImage;

public class GrayscaleFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (r + g + b) / 3;
                int newRgb = (gray << 16) | (gray << 8) | gray;
                grayImage.setRGB(x, y, newRgb);
            }
        }
        return grayImage;
    }
}