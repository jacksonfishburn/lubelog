package dev.jacksonfishburn.lubelog;

import org.springframework.boot.SpringApplication;

public class TestLubelogApplication {

	public static void main(String[] args) {
		SpringApplication.from(LubelogApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
