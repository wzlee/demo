package com.landray.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.landray.demo.entity.Module;

/**
 *
 * @author lizhiwei
 * 2019-02-20 09:27:16
 */
public interface ModuleRepository extends JpaRepository<Module, String> {

}
