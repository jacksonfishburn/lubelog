package dev.jacksonfishburn.lubelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import dev.jacksonfishburn.lubelog.config.RateLimitProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RateLimitProperties.class)
public class LubelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LubelogApplication.class, args);
	}

}
