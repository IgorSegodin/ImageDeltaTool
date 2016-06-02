package org.segodin.imageDeltaTool.service.data;

import java.awt.image.BufferedImage;

public interface ImageData {

    int getRGB(int x, int y);

    int[] getPixel(int x, int y);

    int getWidth();

    int getHeight();

    BufferedImage copyBufferedImage();
}
