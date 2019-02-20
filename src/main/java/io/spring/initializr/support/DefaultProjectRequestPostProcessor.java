package io.spring.initializr.support;
//package com.landray.ops.web.api.provider;
//
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.landray.ops.web.api.entity.project.Module;
//import com.landray.ops.web.api.service.project.ModuleService;
//
//import io.spring.initializr.metadata.Dependency;
//import io.spring.initializr.metadata.InitializrMetadata;
//import io.spring.initializr.web.project.ProjectRequest;
//
///**
// * 默认处理器
// * @author lizhiwei
// * 2019-02-19 13:40:40
// */
//@Component
//public class DefaultProjectRequestPostProcessor implements ProjectRequestPostProcessor {
//
//	@Autowired
//	ModuleService moduleService;
//	
//	@Override
//	public void postProcessAfterResolution(ProjectRequest request, InitializrMetadata metadata) {
//		List<Module> modules = moduleService.findList(request.getDependencies());
//		modules.forEach(module->{
//			Dependency dependency = new Dependency();
//			dependency.setId(module.getId());
//			dependency.setGroupId(module.getGroupId());
//			dependency.setArtifactId(module.getArtifactId());
//			dependency.setDescription(module.getDescription());
//			dependency.setVersion(module.getVersion());
//			request.getResolvedDependencies().add(dependency);
//		});
//	}
//}
