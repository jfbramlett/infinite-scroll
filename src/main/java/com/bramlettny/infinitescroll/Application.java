package com.bramlettny.infinitescroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Our spring boot main entry point.
 */
@SpringBootApplication
public class Application {

	/**
	 * Our main.
	 *
	 * @param args The command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
}
