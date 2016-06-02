package org.segodin.imageDeltaTool.service;

import org.segodin.imageDeltaTool.service.data.ImageData;

import java.awt.image.BufferedImage;

/**
 * Compares images and highlights these differences with rectangles.
 * */
public interface ImageDiffService {

    /**
     * @param origin original image
     * @param toCompare image, containing differences. Copy of this image is used to draw frames with differences.
     * @return image with highlighted differences.
     * */
    BufferedImage getHighlightedImage(ImageData origin, ImageData toCompare);
}
