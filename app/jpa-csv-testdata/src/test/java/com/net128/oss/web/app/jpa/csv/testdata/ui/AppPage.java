package com.net128.oss.web.app.jpa.csv.testdata.ui;

import com.net128.oss.web.lib.support.selenium.BasePage;
import org.springframework.beans.factory.annotation.Value;

public abstract class AppPage extends BasePage {
	@Value("${server.port}")
	private int serverPort;

	public abstract void open();

	public void open(String uri) {
		driver.get(getAppUrl(uri));
	}

	public String getAppUrl(String uri) {
		return "http://localhost:"+serverPort+uri;
	}
}
