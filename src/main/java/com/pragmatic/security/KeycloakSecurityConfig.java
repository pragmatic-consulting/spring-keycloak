package com.pragmatic.security;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class})
@KeycloakConfiguration
@EnableWebSecurity
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * RegisterSessionAuthenticationStrategy is used for public or confidential applications.
	 * 
	 * NullAuthenticatedSessionStrategy is used for bearer-only applications (login from the browser is not allowed).
	 * 
	 * We should discuss this.
	 */
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	/**
	 * Prevent default prefixing with "ROLE_" so the keycloak roles will be the same as the app. 
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(keycloakAuthenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		//enabling post requests without csrf.
		http.cors().and().csrf().disable()
		.authorizeRequests()
		//enabling add user path
		.antMatchers("/add-user/**").permitAll()
		//securing other paths with roles
		.antMatchers("/admin/**").hasAuthority("ADMIN")
		.antMatchers("/manager/**").hasAuthority("MANAGER")
		.anyRequest().authenticated();	
	}

}
