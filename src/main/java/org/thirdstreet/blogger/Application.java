package org.thirdstreet.blogger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;

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
