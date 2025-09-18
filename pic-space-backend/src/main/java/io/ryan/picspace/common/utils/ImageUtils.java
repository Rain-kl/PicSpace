package io.ryan.picspace.common.utils;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class ImageUtils {

    /**
     * 读取图片，返回 BufferedImage
     */
    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        try (InputStream is = inputStream) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("无法读取图片，可能是不支持的格式");
            }
            return image;
        }
    }

    /**
     * 获取图片格式 (jpg/png/webp 等)
     */
    public static String getImageFormat(InputStream inputStream) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(inputStream)) {
            if (iis == null) {
                return "unknown";
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return "unknown";
            }
            ImageReader reader = readers.next();
            try {
                return reader.getFormatName().toLowerCase();
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * 转换图片为 WebP 格式字节数组
     */
    public static byte[] toWebp(byte[] inputBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IllegalArgumentException("无法识别输入图片");
            }

            // 依赖 org.sejda.imageio:webp-imageio，自动注册 WebP writer
            boolean success = ImageIO.write(image, "webp", baos);
            if (!success) {
                throw new IOException("未找到 WebP 写出器，请确认已引入 webp-imageio 依赖");
            }

            return baos.toByteArray();
        }
    }

    /**
     * 保存图片为 WebP 文件
     */
    public static void toWebpFile(File inputFile, File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IllegalArgumentException("无法识别输入图片: " + inputFile.getAbsolutePath());
        }

        boolean success = ImageIO.write(image, "webp", outputFile);
        if (!success) {
            throw new IOException("未找到 WebP 写出器，请确认已引入 webp-imageio 依赖");
        }
    }
}