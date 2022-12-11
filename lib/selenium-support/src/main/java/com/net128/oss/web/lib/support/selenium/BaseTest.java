package com.net128.oss.web.lib.support.selenium;

import com.net128.oss.web.app.jpa.csv.testdata.ui.framework.annotation.LazyAutowired;
import com.net128.oss.web.lib.support.selenium.annotation.SeleniumTest;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

@SeleniumTest
@Getter
public class BaseTest {
    @BeforeEach
    public void setup() {
    }

    @LazyAutowired
    public ApplicationContext applicationContext;

    @AfterEach
    public void teardown() {
        this.applicationContext
            .getBean(WebDriver.class)
            .quit();
    }
}