package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.annotation.LazyComponent;
import lombok.SneakyThrows;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@LazyComponent
public class NavigationPage extends AppPage {

	@FindBy(xpath = "//h3")
	public WebElement title;

	public void open() {
		driver.get(getAppUrl("/index.html"));
	}

	@SneakyThrows
	@Override public boolean isAt() {
		return wait.until((d) -> title.isDisplayed());
	}
}