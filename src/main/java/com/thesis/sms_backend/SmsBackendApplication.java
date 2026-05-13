package com.thesis.sms_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class SmsBackendApplication {

	public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
		SpringApplication.run(SmsBackendApplication.class, args);
	}

}
