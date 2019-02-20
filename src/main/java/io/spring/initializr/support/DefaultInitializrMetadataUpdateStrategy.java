/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landray.demo.entity.Module;
import com.landray.demo.service.ModuleService;

import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * A {@link InitializrMetadataUpdateStrategy} that refreshes the metadata with the status
 * of the main spring.io site.
 *
 * @author Stephane Nicoll
 */
public class DefaultInitializrMetadataUpdateStrategy implements InitializrMetadataUpdateStrategy {

	private static final Log logger = LogFactory
			.getLog(DefaultInitializrMetadataUpdateStrategy.class);

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;
	
	private final ModuleService moduleService;

	public DefaultInitializrMetadataUpdateStrategy(RestTemplate restTemplate,
			ObjectMapper objectMapper,ModuleService moduleService) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.moduleService = moduleService;
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		String url = current.getConfiguration().getEnv().getSpringBootMetadataUrl();
		List<DefaultMetadataElement> bootVersions = fetchSpringBootVersions(url);
		if (bootVersions != null && !bootVersions.isEmpty()) {
			if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
				// No default specified
				bootVersions.get(0).setDefault(true);
			}
			current.updateSpringBootVersions(bootVersions);
		}
		List<DependencyGroup> groups = convertLocalModuleToGroup();
		current.getDependencies().merge(groups);
		logger.debug("依赖库更新成功");
		return current;
	}

	/**
	 * Fetch the available Spring Boot versions using the specified service url.
	 * @param url the url to the spring-boot project metadata
	 * @return the spring boot versions metadata.
	 */
	protected List<DefaultMetadataElement> fetchSpringBootVersions(String url) {
		if (StringUtils.hasText(url)) {
			try {
				logger.info("Fetching Spring Boot metadata from " + url);
				return new SpringBootMetadataReader(this.objectMapper, this.restTemplate,
						url).getBootVersions();
			}
			catch (Exception ex) {
				logger.warn("Failed to fetch Spring Boot metadata", ex);
			}
		}
		return null;
	}
	
	/**
	 * 从数据库中加载所有模块转换成dependencyGroup
	 * @return
	 */
	protected List<DependencyGroup> convertLocalModuleToGroup(){
		List<Module> list = moduleService.list();
		List<DependencyGroup> groups = new ArrayList<>();
		Map<String,List<Module>> map = list.stream().collect(Collectors.groupingBy(Module::getArtifactId));
		map.forEach((artifactid,modules)->{
			DependencyGroup group = new DependencyGroup();
			group.setName(artifactid);
			modules.forEach(module->{
				Dependency dependency = new Dependency();
				dependency.setId(module.getId());
				dependency.setGroupId(module.getGroupId());
				dependency.setArtifactId(module.getArtifactId());
				dependency.setVersion(module.getVersion());
				group.getContent().add(dependency);
			});
			groups.add(group);
		});
		return groups;
	}

}
