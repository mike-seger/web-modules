package com.net128.oss.web.lib.support.selenium.util;

import com.net128.oss.web.webshell.util.TabUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TabUtilTest {
    @Test
    public void test3Columns() {
        String data="C1\tC2\tC3\nasdfasda\tgsdg\tfgsdfg\ndfg\tgsdfgsfdgfdgdfgfgdfg\tgdfg\n";
        String expected =
            " C1       C2                   C3     \n" +
            " asdfasda gsdg                 fgsdfg \n" +
            " dfg      gsdfgsfdgfdgdfgfgdfg gdfg   ";
        String actual = TabUtils.formatFixedWidthColumnsWithBorders(data, false);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test1Column() {
        String data="C1\ngsdg\ngsdfgsfdgfdgdfgfgdfg\n";
        String expected =
            " C1                   \n" +
            " gsdg                 \n" +
            " gsdfgsfdgfdgdfgfgdfg ";
        String actual = TabUtils.formatFixedWidthColumnsWithBorders(data, false);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testTabWithBordersleWithBorders() {
        String data="C1\tC2\tC3\nasdfasda\tgsdg\tfgsdfg\ndfg\tgsdfgsfdgfdgdfgfgdfg\tgdfg\n";
//        String expected =
//                "C1       C2                   C3     \n" +
//                        "asdfasda gsdg                 fgsdfg \n" +
//                        "dfg      gsdfgsfdgfdgdfgfgdfg gdfg   \n";
        String actual = TabUtils.formatFixedWidthColumnsWithBorders(data, true);
//        Assertions.assertEquals(expected, actual);
        System.out.println(actual);

        actual = TabUtils.formatFixedWidthColumnsWithBorders(data, false);
//        Assertions.assertEquals(expected, actual);
        System.out.println(actual);
    }
}
