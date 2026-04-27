package com.thesis.sms_backend;

import org.springframework.boot.SpringApplication;

public class TestSmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmsBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
