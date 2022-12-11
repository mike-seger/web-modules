package com.net128.oss.web.app.jpa.csv.testdata.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class NavigationPageTest extends AppPageTest {
    @Autowired
    private NavigationPage page;

    @Test
    public void openTest() {
        page.open();
        assertTrue(page.isAt());
    }
}