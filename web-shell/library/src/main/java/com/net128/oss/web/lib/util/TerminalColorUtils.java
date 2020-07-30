package com.net128.oss.web.lib.util;

import static com.net128.oss.web.lib.util.TerminalColorUtils.RGB.rgb;

public class TerminalColorUtils {
    public void colorGradientBG(StringBuilder sb, int cols, int rows, RGB from, RGB to) {
        for (int i = 0; i < rows; i++) {
            double weight = i*1.0/rows;
            for (int j = 0; j < cols; j++) {
                setColorBG(sb, from.interpolate(to, weight).scale((j*1.0/(cols-1))));
                sb.append(" ");
            }
            setColorBG(sb, rgb(0, 0, 0));
            setColorFG(sb, rgb(255, 255, 255));
            appendColorInfo(sb, from.interpolate(to, weight));
            for (int j = cols-1; j >= 0; j--) {
                setColorBG(sb, from.interpolate(to, weight).scale(j*1.0/(cols-1)));
                sb.append(" ");
            }
            sb.append("\n");
        }
    }

    public void color256Chart12(StringBuilder sb, int from, int factor, int code) {
        for (int i = 0; i < 12; i++) {
            colored256Content(sb, from + factor*i, code);
        }
        sb.append("\n");
    }

    public void color256Chart2x8(StringBuilder sb, int code) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                colored256Content(sb, i * 8 + j, code);
            }
            sb.append("\n");
        }
    }

    public void color256Chart6x12(StringBuilder sb, int from1, int from2, int factor, int code) {
        for(int i=0; i<6; i++) {
            for (int j = 0; j < 6; j++) {
                colored256Content(sb, from1 + factor*i + j * 6, code);
            }
            for (int j = 0; j < 6; j++) {
                colored256Content(sb, from2 + factor*i - j * 6, code);
            }
            sb.append("\n");
        }
    }

    private void appendColorInfo(StringBuilder sb, RGB color) {
        StringBuilder colorText=new StringBuilder();
        setColorBG(colorText, color);
        sb.append(String.format(" %3s,%3s,%3s ", color.r, color.g, color.b));
    }

    private void colored256Content(StringBuilder sb, int color, int code) {
        sb.append("\u001B[").append(code).append(";5;").append(color).append("m");
        sb.append(String.format("%3s ", "" + color));
    }

    public void colorReset(StringBuilder sb) {
        sb.append("\u001B[0m");
    }

    public void setColorFG(StringBuilder sb, RGB c) {
        setColor(sb, c.r, c.g, c.b, 38);
    }

    public void setColorBG(StringBuilder sb, RGB c) {
        setColor(sb, c.r, c.g, c.b, 48);
    }

    private void setColor(StringBuilder sb, int red, int green, int blue, int type) {
        red=Math.min(Math.max(2,Math.abs(red)),255);
        green=Math.min(Math.max(2,Math.abs(green)),255);
        blue=Math.min(Math.max(2,Math.abs(blue)),255);
        sb.append(String.format("\u001B[%d;2;%d;%d;%d;0m", type, red, green, blue));
    }

    public static class RGB {
        private short r;
        private short g;
        private short b;
        public RGB(double r, double g, double b) {
            this.r=bounded(r);
            this.g=bounded(g);
            this.b=bounded(b);
        }

        public static RGB rgb(double r, double g, double b) {
            return new RGB(r, g, b);
        }

        public RGB interpolate(RGB to, double toWeight) {
            return new RGB(
                    interpolate(r, to.r, toWeight),
                    interpolate(g, to.g, toWeight),
                    interpolate(b, to.b, toWeight)
            );
        }
        private short interpolate(short from, short to, double factor) {
            return bounded(from + (to-from)*factor);
        }

        public RGB scale(double factor) {
            r=scale(r, factor);
            g=scale(g, factor);
            b=scale(b, factor);
            return this;
        }
        private short scale(double scalar, double factor) {
            return bounded(scalar*factor);
        }
        private short bounded(double value) {
            long l=Math.round(value);
            if(l>255) return 255;
            if(l<0) return 0;
            return (short)l;
        }
    }
}
