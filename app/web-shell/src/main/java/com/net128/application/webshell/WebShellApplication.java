package com.net128.application.webshell;

import com.net128.oss.web.webshell.WebShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WebShell.class)
public class WebShellApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(WebShellApplication.class, args);
    }
}
