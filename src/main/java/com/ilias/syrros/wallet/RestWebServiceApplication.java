package com.ilias.syrros.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class RestWebServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestWebServiceApplication.class, args);
	}

}
