package com.pragmatic.security;

import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 
 * @author Alae
 * 
 * WebMVC configuration class
 */
@Configuration
@EnableWebMvc
public class WebConfig {

	@Bean
	KeycloakRestTemplate keycloakRestTemplate(KeycloakClientRequestFactory keycloakClientRequestFactory) {
		return new KeycloakRestTemplate(keycloakClientRequestFactory);
	}
	
}
