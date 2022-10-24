package com.net128.oss.web.app.jpa.csv.testdata;

import com.net128.oss.web.lib.jpa.csv.JpaCsv;
import com.net128.oss.web.lib.jpa.csv.data.test.JpaCsvTestData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({JpaCsv.class, JpaCsvTestData.class})
public class JpaCsvTestDataApplication {
	public static void main(String[] args) {
		SpringApplication.run(JpaCsvTestDataApplication.class, args);
	}
}