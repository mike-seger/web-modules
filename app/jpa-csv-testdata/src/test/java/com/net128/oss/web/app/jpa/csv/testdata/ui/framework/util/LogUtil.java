package com.net128.oss.web.app.jpa.csv.testdata.ui.framework.util;

import com.net128.oss.web.app.jpa.csv.testdata.ui.framework.annotation.LazyComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;

import static org.junit.jupiter.api.Assertions.assertTrue;

@LazyComponent
public class LogUtil {
    public static LogEntries getLogs(WebDriver driver) {
        return driver
            .manage()
            .logs()
            .get(LogType.BROWSER);
    }

    public void isLoginErrorLog(WebDriver driver) {
        //Check logs (works only Chrome and Edge)
        LogEntries logEntries = driver
            .manage()
            .logs()
            .get(LogType.BROWSER);
        assertTrue(logEntries
            .getAll()
            .stream()
            .anyMatch(logEntry -> logEntry
                .getMessage()
                .contains("An invalid email address was specified")));
    }
}