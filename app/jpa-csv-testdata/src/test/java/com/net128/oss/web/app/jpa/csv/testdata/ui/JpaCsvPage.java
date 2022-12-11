package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.annotation.LazyComponent;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.jupiter.api.Assertions.assertEquals;

@LazyComponent
public class JpaCsvPage extends AppPage {

	@FindBy(id = "tb_toolbar_item_entities")
	public WebElement entities;

	@FindBy(xpath = "//div[@class='menu-text'][text() = 'CITY']")
	public WebElement cityEntityMenu;

	@FindBy(id = "grid_grid_data_0_1")
	public WebElement cell1;

	public void open() {
		driver.get(getAppUrl("/jpa-csv/admin/index.html"));
	}

	public void loadCountryEntity() {
		we(entities).click();
		we(cityEntityMenu).click();
		assertEquals(we(cell1).getText(), "Shanghai");
	}

	@Override public boolean isAt() {
		return wait.until((d) -> entities.isDisplayed());
	}
}
