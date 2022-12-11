package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.BasePage;
import com.net128.oss.web.app.jpa.csv.testdata.ui.framework.annotation.LazyComponent;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@LazyComponent
public class IndexPage extends BasePage {

	@FindBy(xpath = "//*[@id='app']/div[1]/div[2]/div[2]/div/div[1]/div[1]/div[1]/h5")
	public WebElement homePageUserName;

	public String getHomePageText() {
		return homePageUserName.getText();
	}

	@Override public boolean isAt() {
		//return this.wait.until((d) -> this.userName.isDisplayed());
		return true;
	}
}