package com.net128.oss.web.app.jpa.csv.testdata.ui.framework.annotation;

import com.net128.oss.web.app.jpa.csv.testdata.JpaCsvTestDataApplication;
import com.net128.oss.web.app.jpa.csv.testdata.ui.framework.BaseTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JpaCsvTestDataApplication.class})
@ComponentScan(basePackageClasses = {JpaCsvTestDataApplication.class, BaseTest.class} )
public @interface SeleniumTest {
}