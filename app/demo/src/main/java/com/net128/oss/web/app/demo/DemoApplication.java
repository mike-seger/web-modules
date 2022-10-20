package com.net128.oss.web.app.demo;

import com.net128.oss.web.lib.filemanager.FileManagerServlet;
import com.net128.oss.web.webshell.WebShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({WebShell.class, FileManagerServlet.class})
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
