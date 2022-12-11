package com.net128.oss.web.app.jpa.csv.testdata;

import com.net128.oss.web.lib.jpa.csv.JpaCsv;
import com.net128.oss.web.lib.jpa.csv.data.test.JpaCsvTestData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.constraints.NotNull;

@SpringBootApplication
@Import({JpaCsv.class, JpaCsvTestData.class, JpaCsvControllerEntityChangeLogger.class})
public class JpaCsvTestDataApplication {
	public static void main(String[] args) {
		SpringApplication.run(JpaCsvTestDataApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NotNull CorsRegistry registry) {
				registry.addMapping("/**").allowedMethods("*");
			}
		};
	}
}
