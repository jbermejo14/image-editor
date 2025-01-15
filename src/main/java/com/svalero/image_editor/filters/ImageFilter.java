package com.svalero.image_editor.filters;

import java.awt.image.BufferedImage;

public interface ImageFilter {
    BufferedImage apply(BufferedImage image);
}