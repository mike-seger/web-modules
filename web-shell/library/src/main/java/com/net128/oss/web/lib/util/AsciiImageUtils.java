package com.net128.oss.web.lib.util;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

@Slf4j
public class AsciiImageUtils {
    private final static TerminalColorUtils tcu = new TerminalColorUtils();

    public static void toAscii8Bit(StringBuilder sb, String imgPath, int maxW, int maxH) {
        toColorString(sb, imgPath, maxW, maxH, new GrayConverter());
    }

    public static void toRgbBg(StringBuilder sb, String imgPath, int maxW, int maxH) {
        toColorString(sb, imgPath, maxW, maxH, new RGBConverter());
    }

    private static void toColorString(StringBuilder sb, String imgPath, int maxW, int maxH, PixelConverter converter) {
        BufferedImage img;
        File file = new File(imgPath);
        try {
            BufferedImage originalImg = ImageIO.read(file);
            Dimension newMaxSize = new Dimension(maxW, maxH);
            img = Scalr.resize(originalImg, Scalr.Method.QUALITY,
                newMaxSize.width, newMaxSize.height);

            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    converter.convert(sb, new Color(img.getRGB(j, i)));
                }
                sb.append("\n");
            }
        } catch (IOException e) {
            String message = String.format("Error processing image from %s", file.getAbsolutePath());
            sb.append(message).append("\n").append(e.getMessage());
            log.error(message, e);
        }
    }

    private interface PixelConverter {
        void convert(StringBuilder sb, Color color);
    }

    private static class GrayConverter implements PixelConverter {
        public void convert(StringBuilder sb, Color color) {
            double grayValue = (((color.getRed() * 0.30) +
                (color.getBlue() * 0.59) + (color.getGreen() * 0.11)));
            sb.append(grayChar(grayValue));
        }
    }

    private static class RGBConverter implements PixelConverter {
        public void convert(StringBuilder sb, Color color) {
            tcu.setColorBG(sb, new TerminalColorUtils.RGB(
                color.getRed(), color.getGreen(), color.getBlue()));
            sb.append(' ');
        }
    }

    private static String grayChar(double g) {
        String str;
        if (g >= 240) {
            str = " ";
        } else if (g >= 210) {
            str = ".";
        } else if (g >= 190) {
            str = "*";
        } else if (g >= 170) {
            str = "+";
        } else if (g >= 120) {
            str = "^";
        } else if (g >= 110) {
            str = "&";
        } else if (g >= 80) {
            str = "8";
        } else if (g >= 60) {
            str = "#";
        } else {
            str = "@";
        }
        return str;
    }
}