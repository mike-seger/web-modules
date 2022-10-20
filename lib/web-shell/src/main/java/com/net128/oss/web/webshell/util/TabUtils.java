package com.net128.oss.web.webshell.util;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.asciitable.CWC_LongestWordMax;
import de.vandermeer.asciithemes.TA_GridThemes;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.*;
import java.util.stream.Collectors;

public class TabUtils {
    public static List<List<String>> fromMatrix(String[][] table) {
        List<List<String>> tableList = new ArrayList<>();
        for (String[] row : table) {
            tableList.add(Arrays.asList(row));
        }
        return tableList;
    }

    public static List<List<String>> fromTabDelim(String tabDelimData) {
        return Arrays.stream(tabDelimData.split("\n"))
            .map(line -> Arrays.asList(line.split("\t")))
            .collect(Collectors.toList());
    }

    public static List<List<String>> sort(List<List<String>> matrix) {
        matrix.sort(new ListComparator<>());
        return matrix;
    }

    public static String formatFixedWidthColumnsWithBorders(String[][] table, boolean withBorders) {
        return formatFixedWidthColumnsWithBorders(fromMatrix(table), withBorders, 100);
    }

    public static String formatFixedWidthColumnsWithBorders(String tabDelimData, boolean withBorders) {
        return formatFixedWidthColumnsWithBorders(fromTabDelim(tabDelimData), withBorders, 100);
    }

    public static String formatFixedWidthColumnsWithBorders(List<List<String>> table, boolean withBorders, int width) {
        if (table.size() == 0 || table.get(0).size() == 0) {
            return "";
        }
        AsciiTable at = new AsciiTable();
        if (withBorders) at.addRule();
        AT_Row row = at.addRow(table.get(0));
        if (withBorders) at.addRule();
        boolean hasTopTitle = false;
        if (countNonNullItems(table.get(0)) == 1 && table.size() > 1 && table.get(1).size() > 1) {
            row.setTextAlignment(TextAlignment.CENTER);
            hasTopTitle = true;
        }
        for (int i = 1; i < table.size(); i++) {
            at.addRow(table.get(i));
            if (hasTopTitle) {
                at.addRule();
                hasTopTitle = false;
            }
        }
        if (withBorders) at.addRule();
        at.getRenderer().setCWC(new CWC_LongestWord());
        if (withBorders) at.setPaddingLeftRight(1);
        else at.getContext().setGridTheme(TA_GridThemes.NONE);
        at.getRenderer().setCWC(new CWC_LongestWordMax((9*width/10)));
        return at.render(width);
    }


    private static class ListComparator<T extends Comparable<T>> implements Comparator<List<T>> {
        @Override
        public int compare(List<T> o1, List<T> o2) {
            for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
                int c = o1.get(i).compareTo(o2.get(i));
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(o1.size(), o2.size());
        }
    }

    private static long countNonNullItems(List<String> list) {
        return list.stream().filter(Objects::nonNull).count();
    }
}
