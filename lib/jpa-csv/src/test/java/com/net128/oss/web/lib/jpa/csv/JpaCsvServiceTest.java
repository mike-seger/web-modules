package com.net128.oss.web.lib.jpa.csv;

import com.net128.oss.web.lib.jpa.csv.common.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = TestApplication.class)
public class JpaCsvServiceTest implements ResourceLoader {
	@SuppressWarnings("unused")
	@Autowired
	private JpaCsvService jpaCsvService;

	@Test
	public void testReadWriteCsv() throws IOException {
		var expected = loadString("/csv/CITY.csv").trim();
		jpaCsvService.readCsv(load("/csv/CITY.csv"), "CITY", false, false);
		var actual = jpaCsvService.writeCsv("CITY", false).trim();
		assertEquals(expected, actual);
	}
}
