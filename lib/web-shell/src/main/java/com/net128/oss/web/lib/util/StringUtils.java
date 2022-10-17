package com.net128.oss.web.lib.util;

import org.fusesource.jansi.Ansi;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class StringUtils {
    public static String noBreak(String s) {
        if(s==null) return null;
        if(s.trim().length()==0) return s;
        return s.replace(" ", "\\");
    }

    public  static String undoNoBreak(String s) {
        if(s==null) return null;
        return s.replace("\\", " ");
    }

    public static String [][] noBreak(String [][] s) {
        for(int i=0; i<s.length; i++) {
            for(int j=0; j<s[i].length; j++) {
                s[i][j] = noBreak(s[i][j]);
            }
        }
        return s;
    }

    public static List<List<String>> noBreak(List<List<String>> listList) {
        for(int i=0; i<listList.size(); i++) {
            for(int j=0; j<listList.get(i).size(); j++) {
                listList.get(i).set(j, noBreak(listList.get(i).get(j)));
            }
        }
        return listList;
    }

    public static int colorizeOne(StringBuilder s, String search, Ansi color, int start) {
        return colorize(s, search, color, false, start);
    }

    public static int colorize(StringBuilder s, String search, Ansi color, boolean all, int start) {
        if(search==null) return start;
        if(search.trim().length()==0) return start+search.length();
        int pos = s.indexOf(search, start);
        while(pos>=0) {
            s.replace(pos, pos + search.length(), color + search + ansi().reset());
            if(!all) break;
            pos = s.indexOf(search);
        }
        if(pos<0) {
            return 0;
        }
        return pos+search.length();
    }

    public static String colorizeFirst(String s, String pattern, Ansi color) {
        return colorizeRegex(s, pattern, color, false);
    }

    public static String colorizeAll(String s, String pattern, Ansi color) {
        return colorizeRegex(s, pattern, color, true);
    }

    public static String colorizeRegex(String s, String pattern, Ansi color, boolean all) {
        if(all) return s.replaceAll(pattern, ""+color+"$1"+ansi().reset());
        return s.replaceFirst(pattern, ""+color+"$1"+ansi().reset());
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static String humanReadableByteCountSIAbbr(Long bytes) {
        return humanReadableByteCountSI(bytes)
            .replace(" ", "")
            .replaceAll("B$", "")
            .replaceAll("\\..", "")
            ;
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
