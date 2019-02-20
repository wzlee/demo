package com.landray.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.landray.demo.entity.Module;
import com.landray.demo.service.ModuleService;

import io.spring.initializr.project.ProjectGenerationInvoker;
import io.spring.initializr.project.ProjectGenerationResult;
import io.spring.initializr.project.ProjectRequest;

/**
 *	Main Controller
 * @author lizhiwei
 * 2019-02-20 09:21:03
 */
@RestController
public class MainController {
	
	@Autowired
	ModuleService moduleService;
	
	@Autowired
	ProjectGenerationInvoker projectGenerationInvoker;
	
	/**
	 * apply module for the app generator
	 * @param module
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/module/apply")
	@ResponseBody
	public ResponseEntity<?> applyModule(@RequestBody Module module,HttpServletRequest request,HttpServletResponse response){
		Module _module = moduleService.save(module);
		// TODO how to add this module to current initializrMetadata's dependencies 
		return ResponseEntity.ok(_module);
	}
	
	/**
	 * generate app with dependencies from the apply module
	 * @param projectRequest
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/app/generate")
	@ResponseBody
	public ResponseEntity<?> generateApp(@RequestBody ProjectRequest projectRequest,HttpServletRequest request,HttpServletResponse response){
		// projectRequest's dependencies property is selected modules's id 
		ProjectGenerationResult result = projectGenerationInvoker.invokeProjectStructureGeneration(projectRequest);
		return ResponseEntity.ok(result.getRootDirectory());
	}
	
}
