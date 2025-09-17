package io.ryan.picspace.common.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ImageUtils {
    public static BufferedImage getImageDimension(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("无法读取图片");
        }
        return image;
    }

    public static String getImageFormat(InputStream inputStream) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(inputStream)) {
            if (iis == null) return "unknown";
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) return "unknown";
            ImageReader reader = iter.next();
            String formatName = reader.getFormatName();
            reader.dispose();
            return formatName.toLowerCase();
        }
    }
}