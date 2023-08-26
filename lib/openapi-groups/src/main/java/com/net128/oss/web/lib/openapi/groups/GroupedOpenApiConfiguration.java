package com.net128.oss.web.lib.openapi.groups;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class GroupedOpenApiConfiguration {
	private final ConfigurableListableBeanFactory beanFactory;
	private final List<String> extraPackagePaths;
	private final String primaryPackageName;
	private final static String DEFAULT_MAIN_GROUP_NAME = "main";
	private final Class<?> mainClass;

	public GroupedOpenApiConfiguration(
		ConfigurableListableBeanFactory beanFactory,
		@Value("${springdoc.swagger-ui.extra-package-paths:}")
		List<String> extraPackagePaths,
		@Value("${springdoc.swagger-ui.urls-primary-name:}")
		String primaryPackageName
	) {
		this.beanFactory = beanFactory;
		this.extraPackagePaths = extraPackagePaths;
		this.primaryPackageName = primaryPackageName;
		this.mainClass = getMainClass();
	}

	private void registerExtraGroupedOpenApis(List<String> packageNames) {
		packageNames.forEach(s -> beanFactory.registerSingleton(
			GroupedOpenApiConfiguration.class.getSimpleName() + "-" + s,
			apiGroup(s)
		));
	}

	private GroupedOpenApi apiGroup(String packageName) {
		log.info("Created GroupedOpenApi: {}", packageName);
		return GroupedOpenApi.builder().group(packageName).packagesToScan(packageName).build();
	}

	public Class<?> getMainClass() {
		try {
			for (StackTraceElement stackTrace : Thread.currentThread().getStackTrace()) {
				if ("main".equals(stackTrace.getMethodName())) {
					try {
						return Class.forName(stackTrace.getClassName());
					} catch (ClassNotFoundException e) {
						break;
					}
				}
			}
			throw new ClassNotFoundException();
		} catch (ClassNotFoundException e) {
			log.error("No SpringBootApplication annotated class found- Returning null");
			return getClass();
		}
	}

	@Bean
	@Primary
	public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties config) {
		config.setUrlsPrimaryName(primaryPackageName);
		return config;
	}

	@Bean
	public GroupedOpenApi mainApi() {
		var packageNames = packageNames();
		if(packageNames.isEmpty()) {
			log.error("No controller package classes found under: {}", mainClass.getPackageName());
			return apiGroup(DEFAULT_MAIN_GROUP_NAME);
		}

		var mainPackage = packageNames.remove(0);
		registerExtraGroupedOpenApis(packageNames);
		return apiGroup(mainPackage);
	}

	private List<String> packageNames() {
		var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

		var extraControllers = extraPackagePaths.stream().map(scanner::findCandidateComponents).flatMap(Collection::stream).map(c -> {
			try { return Class.forName(c.getBeanClassName());
			} catch (ClassNotFoundException e) { throw new RuntimeException(e); }
		}).collect(Collectors.toList());

		var controllerClasses = new Reflections(mainClass.getPackageName()).getTypesAnnotatedWith(RestController.class);
		controllerClasses.addAll(extraControllers);
		log.info("RestControllers found: {}", controllerClasses);
		return controllerClasses.stream().map(Class::getPackageName).distinct().collect(Collectors.toList());
	}
}
