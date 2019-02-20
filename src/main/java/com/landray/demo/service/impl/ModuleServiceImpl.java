package com.landray.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.landray.demo.entity.Module;
import com.landray.demo.repository.ModuleRepository;
import com.landray.demo.service.ModuleService;

/**
 * implement the interface
 * @author lizhiwei
 * 2019-02-20 09:13:26
 */
@Service
public class ModuleServiceImpl implements ModuleService {

	@Autowired
	ModuleRepository moduleRepository;
	
	@Override
	public Module save(Module module) {
		return moduleRepository.save(module);
	}

	@Override
	public List<Module> list() {
		return moduleRepository.findAll();
	}

}
