package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.annotation.LazyComponent;
import org.junit.jupiter.api.Disabled;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@LazyComponent
public class JpaCsvPage extends AppPage {

	@FindBy(id = "tb_toolbar_item_entities")
	public WebElement entities;

	@FindBy(xpath = "//div[@class='menu-text'][text() = 'CITY']")
	public WebElement cityEntityMenu;

	@FindBy(id = "grid_grid_data_0_1")
	public WebElement cell1;

	@FindBy(id = "grid_grid_data_1_1")
	public WebElement cell2;

	@FindBy(xpath = "//div[@class='w2ui-menu-item'][div[@class='menu-text'][text() = 'Insert new rows before']]")
	public WebElement insertNewRowsBefore;

	public void open() {
		driver.get(getAppUrl("/jpa-csv/admin/index.html"));
	}

	public void loadCityEntity() {
		w4(entities).click();
		w4(cityEntityMenu).click();
		assertEquals("Shanghai", w4(cell1).getText());
		assertEquals("Beijing", w4(cell2).getText());
	}

	public void insertNewRow(List<String> cellValues) {
		Actions actions = new Actions(driver);
		actions.contextClick(cell2).perform();
		w4(insertNewRowsBefore).click();
		assertEquals("", w4(cell2).getText());
		w4(cell2).click();
		enterRowValues(cellValues);
		//assertEquals(cellValues.get(0), /*cell2.getText()*/""); //FIXME
	}

	private void enterRowValues(List<String> cellValues) {
		var actions = new Actions(driver);
		for(var value : cellValues) {
			if(value.length()>0) sendKeys(actions, value, 50);
			sendKeys(actions, Keys.ENTER, 80);
			sendKeys(actions, Keys.ARROW_UP, 80);
			sendKeys(actions, Keys.ARROW_RIGHT, 80);
		}
	}

	private void sendKeys(Actions actions, CharSequence keys, int delay)  {
		sleep(delay);
		actions.sendKeys(keys).build().perform();
	}

	private void sleep(int delay) {
		try { Thread.sleep(delay); }
		catch (InterruptedException e) { /* we don't care */ }
	}

	@Override public boolean isAt() {
		return wait.until((d) -> entities.isDisplayed());
	}
}
