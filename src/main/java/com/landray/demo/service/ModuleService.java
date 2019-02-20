package com.landray.demo.service;

import java.util.List;

import com.landray.demo.entity.Module;

/**
 * module service interface
 * 
 * @author lizhiwei 2019-02-20 09:11:46
 */
public interface ModuleService {
	
	Module save(Module module);
	
	List<Module> list();
	
}
