package com.net128.oss.web.app.jpa.csv.testdata.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class JpaCsvPageTest extends AppPageTest {
    @Autowired
    private JpaCsvPage page;

    @Test
    public void openTest() {
        page.open();
        assertTrue(page.isAt());
    }

    @Test
    public void loadEntityTest() {
        page.open();
        page.loadCityEntity();
        page.insertNewRow(List.of("NewCity\t10000\tChina\tCN\t10\t10\t1234\t2024-01-01".split("\t")));
    }
}