package com.vitorsaucedo.janus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JanusApplication {

	public static void main(String[] args) {
		SpringApplication.run(JanusApplication.class, args);
	}
}
