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

import com.landray.demo.entity.Module;

import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * update initializrMeta dependencies
 * But no work
 * @author lizhiwei
 * 2019-02-19 16:50
 */
@Slf4j
public class DependencyInitializrMetadataUpdateStrategy implements InitializrMetadataUpdateStrategy {

	private final InitializrMetadataBuilder builder = InitializrMetadataBuilder.create();
	
	private final Module module;
	
	public DependencyInitializrMetadataUpdateStrategy(Module module) {
		this.module = module;
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		builder.withCustomizer((it) -> {
			DependencyGroup group = new DependencyGroup();
			group.setName(module.getArtifactId());
			Dependency dependency = new Dependency();
			dependency.setId(module.getId());
			dependency.setGroupId(module.getGroupId());
			dependency.setArtifactId(module.getArtifactId());
			dependency.setVersion(module.getVersion());
			group.getContent().add(dependency);
			it.getDependencies().getContent().add(group);
		});
		current.getDependencies().merge(builder.build().getDependencies());
		if(log.isDebugEnabled()) {
			log.debug("模块[{}]成功加载到依赖库", module);
		}
		return current;
	}

}
