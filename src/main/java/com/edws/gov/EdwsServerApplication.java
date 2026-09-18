package com.edws.gov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;

/**
 * Thymeleaf's auto-configuration is excluded on purpose.
 *
 * <p>This service returns JSON only and never renders a Thymeleaf view; the engine is used
 * solely to build notification bodies, and {@code NotificationConfig} declares those two
 * engines itself. Left enabled, the auto-configuration also registers a
 * {@code ThymeleafViewResolver} that asks for exactly one {@code SpringTemplateEngine} bean
 * and would fail at startup because there are two (HTML and text).</p>
 */
@SpringBootApplication(exclude = ThymeleafAutoConfiguration.class)
public class EdwsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdwsServerApplication.class, args);
	}

}
