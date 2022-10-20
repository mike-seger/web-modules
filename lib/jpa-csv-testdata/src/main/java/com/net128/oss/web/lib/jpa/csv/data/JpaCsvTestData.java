package com.net128.oss.web.lib.jpa.csv.data;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackageClasses = JpaCsvTestData.class)
@EntityScan(basePackageClasses = JpaCsvTestData.class)
@EnableJpaRepositories(basePackageClasses = JpaCsvTestData.class)
public class JpaCsvTestData {
}
