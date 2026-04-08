package com.net128.oss.web.app.filemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.net128.oss.web.app.filemanager",
	"com.net128.oss.web.lib.filemanager"
})
public class FileManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileManagerApplication.class, args);
	}
}
