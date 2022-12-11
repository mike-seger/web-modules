package com.net128.oss.web.app.jpa.csv.testdata.ui;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
@Slf4j
public class IndexPageTest extends AppPageTest {
    @Autowired
    private IndexPage indexPage;

    @Test
    public void openTest() {
        indexPage.open();
        assertTrue(indexPage.isAt());
    }
}