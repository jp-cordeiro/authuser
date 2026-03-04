package com.ead.authuser;

import org.springframework.boot.SpringApplication;

public class TestAuthuserApplication {

	public static void main(String[] args) {
		SpringApplication.from(AuthuserApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
