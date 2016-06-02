package org.segodin.imageDeltaTool.util;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageUtil {

    public static boolean notEquals(BufferedImage a, BufferedImage b) {
        return ! equals(a, b);
    }

    public static boolean equals(BufferedImage a, BufferedImage b) {
        if (a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight()) {
            int width = a.getWidth();
            int height = a.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (a.getRGB(x, y) != b.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static String getImageFormatName(File image) {
        return image.getName().substring(image.getName().lastIndexOf(".") + 1);
    }
}
