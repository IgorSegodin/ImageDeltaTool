package org.segodin.imageDeltaTool.service.data;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class BufferedImageData implements ImageData {

    private BufferedImage image;

    public BufferedImageData(BufferedImage image) {
        this.image = image;
    }

    @Override
    public int getRGB(int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public int[] getPixel(int x, int y) {
        return image.getRaster().getPixel(x, y, (int[])null);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public BufferedImage copyBufferedImage() {
        ColorModel target = image.getColorModel();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(target, raster, target.isAlphaPremultiplied(), null);
    }
}
