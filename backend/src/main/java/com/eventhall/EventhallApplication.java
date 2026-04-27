package com.eventhall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * This is the main entry point of the Spring Boot application.
 *
 * When you press Run in IntelliJ, this is the class that starts everything:
 * - Spring starts an embedded web server.
 * - Spring scans your project for components like controllers, services, and repositories.
 * - Spring creates the objects it needs and connects them together using dependency injection.
 */
@SpringBootApplication
public class EventhallApplication {

	public static void main(String[] args) {
		/*
		 * SpringApplication.run(...) starts the full Spring Boot application.
		 *
		 * EventhallApplication.class tells Spring where the application starts.
		 * Because this class is in package com.eventhall, Spring will scan this package
		 * and all subpackages such as controller, service, repository, entity, dto, etc.
		 */
		SpringApplication.run(EventhallApplication.class, args);
	}
}