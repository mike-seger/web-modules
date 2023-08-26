package com.net128.oss.web.lib.openapi.groups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import otherpackage.OtherController;

@SpringBootApplication
@Import({
	GroupedOpenApiConfiguration.class,
	OtherController.class
})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
