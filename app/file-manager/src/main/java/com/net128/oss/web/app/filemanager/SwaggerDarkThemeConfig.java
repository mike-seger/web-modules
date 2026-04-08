package com.net128.oss.web.app.filemanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

@Configuration
public class SwaggerDarkThemeConfig {

	private static final String CUSTOM_CSS_URL = "/swagger-dark.css";

	@Bean
	SwaggerIndexTransformer indexPageTransformer(
			SwaggerUiConfigProperties swaggerUiConfig,
			SwaggerUiOAuthProperties swaggerUiOAuthProperties,
			SwaggerWelcomeCommon swaggerWelcomeCommon,
			ObjectMapperProvider objectMapperProvider) {
		return new SwaggerIndexPageTransformer(
				swaggerUiConfig, swaggerUiOAuthProperties,
				swaggerWelcomeCommon, objectMapperProvider) {
			@Override
			public Resource transform(HttpServletRequest request, Resource resource,
					ResourceTransformerChain transformerChain) throws IOException {
				Resource transformed = super.transform(request, resource, transformerChain);
				if (transformed instanceof TransformedResource tr) {
					String js = new String(tr.getByteArray(), StandardCharsets.UTF_8);
					js = js.replace(
							"window.ui = SwaggerUIBundle({",
							"window.ui = SwaggerUIBundle({\n\t\tcustomCssUrl: \"" + CUSTOM_CSS_URL + "\","
					);
					return new TransformedResource(resource,
							js.getBytes(StandardCharsets.UTF_8));
				}
				return transformed;
			}
		};
	}
}

