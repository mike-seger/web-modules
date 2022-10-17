package com.net128.oss.web.lib.commands.picocli;

import static com.net128.oss.web.lib.util.TerminalColorUtils.RGB.rgb;

import com.net128.oss.web.lib.util.AsciiImageUtils;
import com.net128.oss.web.lib.util.TerminalColorUtils;
import picocli.CommandLine.Command;

/**
 * Terminal  commands
 */
public class TerminalCommands {
    private final TerminalColorUtils tcu=new TerminalColorUtils();

    @Command(description = "Display an image file as ASCII text", mixinStandardHelpOptions = true)
    public String image2Ascii(String imageFile, int width, int height) {
        StringBuilder sb = new StringBuilder();
        AsciiImageUtils.toAscii8Bit(sb, imageFile, width, height);
        return sb.toString();
    }

    @Command(description = "Display an image file as colored background space characters", mixinStandardHelpOptions = true)
    public String image2RgbBg(String imageFile, int width, int height) {
        StringBuilder sb = new StringBuilder();
        AsciiImageUtils.toRgbBg(sb, imageFile, width, height);
        return sb.toString();
    }

    @Command(description = "Display 256 terminal color chart", mixinStandardHelpOptions = true)
    public String colors256() {
        StringBuilder sb=new StringBuilder();
        for(int code : new int[]{38, 48}) {
            sb.append("\n");
            tcu.colorReset(sb);
            sb.append("\u001B[1m");
            sb.append(code==38?"Foregrounds":"Backgrounds");
            sb.append(" \\e[").append(code).append(";5;{color}m\n");
            tcu.colorReset(sb);
            tcu.color256Chart6x12(sb, 16, 82, 1, code);
            tcu.color256Chart6x12(sb, 93, 159, -1, code);
            tcu.color256Chart6x12(sb, 160, 226, 1, code);
            tcu.color256Chart12(sb, 232, 1, code);
            tcu.color256Chart12(sb, 255, -1, code);
            tcu.color256Chart2x8(sb, code);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Command(description = "Display RGB terminal color chart", mixinStandardHelpOptions = true)
    public String colorsRGB() {
        StringBuilder sb = new StringBuilder();
        int cols=36;
        int rows=6;
        sb.append("Backgrounds: \\e[48;2;{red};{green};{blue};0m\n");
        tcu.colorReset(sb);
        tcu.colorGradientBG(sb, cols, rows, rgb(255,0,0), rgb(255,255,0));
        tcu.colorGradientBG(sb, cols, rows, rgb(255,255,0), rgb(0,255,0));
        tcu.colorGradientBG(sb, cols, rows, rgb(0, 255,0), rgb(0, 255,255));
        tcu.colorGradientBG(sb, cols, rows, rgb(0, 255,255), rgb(0, 0, 255));
        tcu.colorGradientBG(sb, cols, rows, rgb(0, 0,255), rgb(255, 0, 255));
        tcu.colorGradientBG(sb, cols, rows, rgb(255, 0,255), rgb(255, 0, 0));
        return sb.toString();
    }
}
