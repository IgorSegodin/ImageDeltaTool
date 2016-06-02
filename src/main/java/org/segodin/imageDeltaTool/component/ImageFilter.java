package org.segodin.imageDeltaTool.component;


import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ImageFilter extends FileFilter {

    protected List<String> acceptExtensions = Arrays.asList("jpeg", "jpg", "gif", "tiff", "tif", "png");

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);

        return extension != null && acceptExtensions.contains(extension.toLowerCase());
    }

    public String getDescription() {
        return "Images (" + String.join(", ", acceptExtensions) + ")";
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}