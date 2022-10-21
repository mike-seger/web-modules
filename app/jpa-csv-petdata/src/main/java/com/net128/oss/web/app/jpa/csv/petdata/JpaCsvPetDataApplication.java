package com.net128.oss.web.app.jpa.csv.petdata;

import com.net128.oss.web.lib.jpa.csv.JpaCsv;
import com.net128.oss.web.lib.jpa.csv.pet.JpaCsvPetData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({JpaCsv.class, JpaCsvPetData.class})
public class JpaCsvPetDataApplication {
	public static void main(String[] args) {
		SpringApplication.run(JpaCsvPetDataApplication.class, args);
	}
}
