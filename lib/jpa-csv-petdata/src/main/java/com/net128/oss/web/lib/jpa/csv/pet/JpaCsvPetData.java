package com.net128.oss.web.lib.jpa.csv.pet;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackageClasses = JpaCsvPetData.class)
@EntityScan(basePackageClasses = JpaCsvPetData.class)
@EnableJpaRepositories(basePackageClasses = JpaCsvPetData.class)
public class JpaCsvPetData {
}
