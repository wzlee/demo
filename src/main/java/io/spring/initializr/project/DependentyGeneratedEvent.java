package io.spring.initializr.project;

import com.fasterxml.jackson.databind.Module;

import io.spring.initializr.metadata.InitializrMetadata;

/**
 * 模块包上传事件
 * @author lizhiwei
 *
 */
public abstract class DependentyGeneratedEvent {

	private final Module module;

	private final InitializrMetadata metadata;

	protected DependentyGeneratedEvent(Module module, InitializrMetadata metadata) {
		this.module = module;
		this.metadata = metadata;
	}

	public Module getModule() {
		return module;
	}

	public InitializrMetadata getMetadata() {
		return metadata;
	}

}
