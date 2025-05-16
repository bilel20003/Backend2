package com.centre.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.centre.service")
@EnableJpaRepositories(basePackages = "com.centre.service.repository")
public class CentreServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CentreServiceApplication.class, args);
	}
}