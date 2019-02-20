package com.landray.demo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan("com.landray.demo.entity")
@EnableTransactionManagement
public class JpaConfig {
    
}
