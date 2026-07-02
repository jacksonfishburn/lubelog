package dev.jacksonfishburn.lubelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LubelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LubelogApplication.class, args);
	}

}
