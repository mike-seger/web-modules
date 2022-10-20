package com.net128.oss.web.webshell;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ComponentScan(basePackageClasses = WebShell.class)
public class WebShell {
	@Configuration
	@ConfigurationProperties(prefix = "web-shell")
	@Data
	public static class WebShellConfiguration {
		private List<String> hostShells = List.of("zsh","bash","sh");
		private String root = "/web-shell";
	}
}
