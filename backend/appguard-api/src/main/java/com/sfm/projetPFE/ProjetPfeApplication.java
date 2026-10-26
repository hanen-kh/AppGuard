package com.sfm.projetPFE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableScheduling
@EnableRedisRepositories(basePackages = "com.sfm.projetPFE.AppGuardLogs")
public class ProjetPfeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetPfeApplication.class, args);



	}



}
