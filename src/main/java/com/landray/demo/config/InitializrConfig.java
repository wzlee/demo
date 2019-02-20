package com.landray.demo.config;

import java.nio.file.Files;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landray.demo.service.ModuleService;

import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.io.template.TemplateRenderer;
import io.spring.initializr.generator.project.ProjectDirectoryFactory;
import io.spring.initializr.metadata.DependencyMetadataProvider;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.project.ProjectGenerationInvoker;
import io.spring.initializr.project.ProjectRequestToDescriptionConverter;
import io.spring.initializr.serializer.InitializrModule;
import io.spring.initializr.support.DefaultDependencyMetadataProvider;
import io.spring.initializr.support.DefaultInitializrMetadataProvider;
import io.spring.initializr.support.DefaultInitializrMetadataUpdateStrategy;
import io.spring.initializr.support.DefaultInitializrMetadatabuilder;
import io.spring.initializr.support.InitializrMetadataUpdateStrategy;

/**
 * initializr config
 * 
 * @author lizhiwei 
 * 2019-02-19 15:43:38
 */
@Configuration
@EnableConfigurationProperties(InitializrProperties.class)
@AutoConfigureAfter({ JacksonAutoConfiguration.class, RestTemplateAutoConfiguration.class })
public class InitializrConfig {

	@Autowired
	ModuleService moduleService;
	
	@Bean
	public ProjectDirectoryFactory projectDirectoryFactory() {
		return (description) -> Files.createTempDirectory("project-");
	}

	@Bean
	public IndentingWriterFactory indentingWriterFactory() {
		return IndentingWriterFactory.create(new SimpleIndentStrategy("\t"));
	}

	@Bean
	public TemplateRenderer templateRenderer(Environment environment, ObjectProvider<CacheManager> cacheManager) {
		return new MustacheTemplateRenderer("classpath:/templates",
				determineCache(environment, cacheManager.getIfAvailable()));
	}

	private Cache determineCache(Environment environment, CacheManager cacheManager) {
		if (cacheManager != null) {
			Binder binder = Binder.get(environment);
			boolean cache = binder.bind("spring.mustache.cache", Boolean.class).orElse(true);
			if (cache) {
				return cacheManager.getCache("initializr.templates");
			}
		}
		return new NoOpCache("templates");
	}

	@Bean
	public InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy(RestTemplateBuilder restTemplateBuilder,
			ObjectMapper objectMapper) {
		return new DefaultInitializrMetadataUpdateStrategy(restTemplateBuilder.build(), objectMapper,moduleService);
	}
	
	@Bean
	public InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties,
			InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy) {
		// load all moudle to initializrmetadata's dependencie from database when start
		InitializrMetadata metadata = DefaultInitializrMetadatabuilder.withLocalModules(moduleService).build();
		return new DefaultInitializrMetadataProvider(metadata, initializrMetadataUpdateStrategy);
	}

	@Bean
	public DependencyMetadataProvider dependencyMetadataProvider() {
		return new DefaultDependencyMetadataProvider();
	}

	/**
	 * Initializr web configuration.
	 */
	@Configuration
	static class InitializrWebConfiguration {

		@Bean
		public ProjectGenerationInvoker projectGenerationInvoker(ApplicationContext applicationContext,
				ApplicationEventPublisher eventPublisher,
				ProjectRequestToDescriptionConverter projectRequestToDescriptionConverter) {
			return new ProjectGenerationInvoker(applicationContext, eventPublisher,
					projectRequestToDescriptionConverter);
		}

		@Bean
		public ProjectRequestToDescriptionConverter projectRequestToDescriptionConverter() {
			return new ProjectRequestToDescriptionConverter();
		}

		@Bean
		public InitializrModule InitializrJacksonModule() {
			return new InitializrModule();
		}

	}

	/**
	 * Initializr cache configuration.
	 */
	@Configuration
	static class InitializrCacheConfiguration {

		@Bean
		public JCacheManagerCustomizer initializrCacheManagerCustomizer() {
			return (cacheManager) -> {
				cacheManager.createCache("initializr.metadata",
						config().setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES)));
				cacheManager.createCache("initializr.dependency-metadata", config());
				cacheManager.createCache("initializr.project-resources", config());
				cacheManager.createCache("initializr.templates", config());
			};
		}

		private MutableConfiguration<Object, Object> config() {
			return new MutableConfiguration<>().setStoreByValue(false).setManagementEnabled(true)
					.setStatisticsEnabled(true);
		}

	}
}
