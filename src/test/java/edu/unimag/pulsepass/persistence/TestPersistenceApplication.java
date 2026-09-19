package edu.unimag.pulsepass.persistence;

import org.springframework.boot.SpringApplication;

public class TestPersistenceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PersistenceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
