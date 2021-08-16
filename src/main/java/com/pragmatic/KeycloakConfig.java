package com.pragmatic;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

public class KeycloakConfig {

	static Keycloak keycloak = null;
	final static String serverUrl = "http://localhost:8080/auth";
	final static String realm = "poc-realm";
	final static String clientId = "first-application";
	final static String clientSecret = "e597a6f0-506b-4b54-928a-e5fd2ed49617";
	final static String userName = "admin";
	final static String password = "admin1234";

	public KeycloakConfig() {
	}

	public static Keycloak getInstance(){
	        if(keycloak == null){
	           
	            keycloak = KeycloakBuilder.builder()
	                    .serverUrl(serverUrl)
	                    .realm(realm)
	                    .grantType(OAuth2Constants.PASSWORD)
	                    .username(userName)
	                    .password(password)
	                    .clientId(clientId)
	                    .clientSecret(clientSecret)
	                    .resteasyClient(new ResteasyClientBuilder()
	                                   .connectionPoolSize(10)
	                                   .build()
	                                   )
	                    .build();
	        }
	        return keycloak;
	    }
}
