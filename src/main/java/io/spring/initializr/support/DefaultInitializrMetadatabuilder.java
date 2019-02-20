package io.spring.initializr.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.springframework.util.StringUtils;

import com.landray.demo.service.ModuleService;

import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.Repository;
import io.spring.initializr.metadata.Type;
import io.spring.initializr.metadata.InitializrConfiguration.Env.Kotlin;
import io.spring.initializr.metadata.InitializrConfiguration.Env.Maven.ParentPom;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建默认的metadata
 * @author lizhiwei
 * 2019-01-29 14:35:00
 */
@Slf4j
public class DefaultInitializrMetadatabuilder {
	
	private final InitializrMetadataBuilder builder = InitializrMetadataBuilder.create();
	
	public static DefaultInitializrMetadatabuilder withDefaults() {
		return new DefaultInitializrMetadatabuilder().addAllDefaults();
	}

	public static DefaultInitializrMetadatabuilder withBasicDefaults() {
		return new DefaultInitializrMetadatabuilder().addBasicDefaults();
	}
	
	public static DefaultInitializrMetadatabuilder withLocalModules(ModuleService moduleService) {
		return new DefaultInitializrMetadatabuilder().addBasicDefaults().addLocalModules(moduleService);
	}

	public InitializrMetadata build() {
		return this.builder.build();
	}

	public DefaultInitializrMetadatabuilder addDependencyGroup(String name, String... ids) {
		this.builder.withCustomizer((it) -> {
			DependencyGroup group = new DependencyGroup();
			group.setName(name);
			for (String id : ids) {
				Dependency dependency = new Dependency();
				dependency.setId(id);
				group.getContent().add(dependency);
			}
			it.getDependencies().getContent().add(group);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addDependencyGroup(String name,
			Dependency... dependencies) {
		this.builder.withCustomizer((it) -> {
			DependencyGroup group = new DependencyGroup();
			group.setName(name);
			group.getContent().addAll(Arrays.asList(dependencies));
			it.getDependencies().getContent().add(group);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addAllDefaults() {
		return addBasicDefaults().setGradleEnv("0.5.1.RELEASE").setKotlinEnv("1.1.1");
	}

	public DefaultInitializrMetadatabuilder addBasicDefaults() {
		return addDefaultTypes().addDefaultPackagings().addDefaultJavaVersions()
				.addDefaultLanguages().addDefaultBootVersions();
	}
	
	/**
	 * 添加模块至依赖库
	 * @return DefaultInitializrMetadatabuilder
	 */
	public DefaultInitializrMetadatabuilder addLocalModules(ModuleService moduleService) {
		// TODO 新增模块实现热加载
		moduleService.list().forEach(module->{
			Dependency dependency = new Dependency();
			dependency.setId(module.getId());
			dependency.setGroupId(module.getGroupId());
			dependency.setArtifactId(module.getArtifactId());
			dependency.setDescription(module.getDescription());
			dependency.setVersion(module.getVersion());
			addDependencyGroup(module.getArtifactId(),dependency);
			if(log.isDebugEnabled()) {
				log.debug("添加模块[{}]依赖库",dependency);
			}
		});
		addDependencyGroup("CORE",Dependency.withId("web","org.springframework.boot", "spring-boot-starter-web"));
		return this;
	}

	public DefaultInitializrMetadatabuilder addDefaultTypes() {
		return addType("maven-build", false, "/pom.xml", "maven", "build")
				.addType("maven-project", true, "/starter.zip", "maven", "project")
				.addType("gradle-build", false, "/build.gradle", "gradle", "build")
				.addType("gradle-project", false, "/starter.zip", "gradle", "project");
	}

	public DefaultInitializrMetadatabuilder addType(String id, boolean defaultValue,
			String action, String build, String format) {
		Type type = new Type();
		type.setId(id);
		type.setName(id);
		type.setDefault(defaultValue);
		type.setAction(action);
		if (StringUtils.hasText(build)) {
			type.getTags().put("build", build);
		}
		if (StringUtils.hasText(format)) {
			type.getTags().put("format", format);
		}
		return addType(type);
	}

	public DefaultInitializrMetadatabuilder addType(Type type) {
		this.builder.withCustomizer((it) -> it.getTypes().getContent().add(type));
		return this;
	}

	public DefaultInitializrMetadatabuilder addDefaultPackagings() {
		return addPackaging("jar", true).addPackaging("war", false);
	}

	public DefaultInitializrMetadatabuilder addPackaging(String id, boolean defaultValue) {
		this.builder.withCustomizer((it) -> {
			DefaultMetadataElement packaging = new DefaultMetadataElement();
			packaging.setId(id);
			packaging.setName(id);
			packaging.setDefault(defaultValue);
			it.getPackagings().getContent().add(packaging);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addDefaultJavaVersions() {
		return addJavaVersion("1.6", false).addJavaVersion("1.7", false)
				.addJavaVersion("1.8", true);
	}

	public DefaultInitializrMetadatabuilder addJavaVersion(String version,
			boolean defaultValue) {
		this.builder.withCustomizer((it) -> {
			DefaultMetadataElement element = new DefaultMetadataElement();
			element.setId(version);
			element.setName(version);
			element.setDefault(defaultValue);
			it.getJavaVersions().getContent().add(element);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addDefaultLanguages() {
		return addLanguage("java", true).addLanguage("groovy", false)
				.addLanguage("kotlin", false);
	}

	public DefaultInitializrMetadatabuilder addLanguage(String id, boolean defaultValue) {
		this.builder.withCustomizer((it) -> {
			DefaultMetadataElement element = new DefaultMetadataElement();
			element.setId(id);
			element.setName(id);
			element.setDefault(defaultValue);
			it.getLanguages().getContent().add(element);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addDefaultBootVersions() {
		return addBootVersion("1.5.17.RELEASE", false)
				.addBootVersion("2.0.3.RELEASE", false)
				.addBootVersion("2.1.1.RELEASE", true)
				.addBootVersion("2.2.0.BUILD-SNAPSHOT", false);
	}

	public DefaultInitializrMetadatabuilder addBootVersion(String id, boolean defaultValue) {
		this.builder.withCustomizer((it) -> {
			DefaultMetadataElement element = new DefaultMetadataElement();
			element.setId(id);
			element.setName(id);
			element.setDefault(defaultValue);
			it.getBootVersions().getContent().add(element);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addBom(String id, String groupId,
			String artifactId, String version) {
		BillOfMaterials bom = BillOfMaterials.create(groupId, artifactId, version);
		return addBom(id, bom);
	}

	public DefaultInitializrMetadatabuilder addBom(String id, BillOfMaterials bom) {
		this.builder.withCustomizer(
				(it) -> it.getConfiguration().getEnv().getBoms().put(id, bom));
		return this;
	}

	public DefaultInitializrMetadatabuilder setGradleEnv(
			String dependencyManagementPluginVersion) {
		this.builder.withCustomizer((it) -> it.getConfiguration().getEnv().getGradle()
				.setDependencyManagementPluginVersion(dependencyManagementPluginVersion));
		return this;
	}

	public DefaultInitializrMetadatabuilder setKotlinEnv(String defaultKotlinVersion,
			Kotlin.Mapping... mappings) {
		this.builder.withCustomizer((it) -> {
			it.getConfiguration().getEnv().getKotlin()
					.setDefaultVersion(defaultKotlinVersion);
			for (Kotlin.Mapping mapping : mappings) {
				it.getConfiguration().getEnv().getKotlin().getMappings().add(mapping);
			}
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder setMavenParent(String groupId, String artifactId,
			String version, boolean includeSpringBootBom) {
		this.builder.withCustomizer((it) -> {
			ParentPom parent = it.getConfiguration().getEnv().getMaven().getParent();
			parent.setGroupId(groupId);
			parent.setArtifactId(artifactId);
			parent.setVersion(version);
			parent.setIncludeSpringBootBom(includeSpringBootBom);
		});
		return this;
	}

	public DefaultInitializrMetadatabuilder addRepository(String id, String name, String url,
			boolean snapshotsEnabled) {
		this.builder.withCustomizer((it) -> {
			Repository repo = new Repository();
			repo.setName(name);
			try {
				repo.setUrl(new URL(url));
			}
			catch (MalformedURLException ex) {
				throw new IllegalArgumentException("Cannot create URL", ex);
			}
			repo.setSnapshotsEnabled(snapshotsEnabled);
			it.getConfiguration().getEnv().getRepositories().put(id, repo);
		});
		return this;
	}
	
}
