package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.BasePage;
import com.net128.oss.web.lib.support.selenium.annotation.LazyComponent;
import lombok.SneakyThrows;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@LazyComponent
public class IndexPage extends AppPage {

	@FindBy(xpath = "//h3")
	public WebElement title;

	public void open() {
		driver.get(getAppUrl("/index.html"));
	}

	@SneakyThrows
	@Override public boolean isAt() {
		return wait.withTimeout(Duration.of(1, ChronoUnit.SECONDS)).until((d) -> title.isDisplayed());
	}
}