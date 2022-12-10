package com.net128.oss.web.lib.jpa.csv;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = TestApplication.class)
public class JpaCsvServiceTest {
	@Autowired
	private JpaCsvService jpaCsvService;

	@Test
	public void testReadCsv() throws IOException {
		jpaCsvService.readCsv(getClass().getResourceAsStream("/csv//CITY.csv"), "CITY", false, false);
	}
}
