package com.pragmatic;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.annotation.Order;

public class KeycloakConfigTest {
	
	
	UserServiceInterace userServiceInterace;
	
	public static User user;
	public static UserRepresentation addedUser;

    private static UsersResource usersResource;
    
    public KeycloakConfigTest() {
    	this.userServiceInterace = new UserService();
    }
    	
    @BeforeClass
    public static void initUser() {
    	user = new User("user1", "last1", "pass1", "user1@last1.com");
    }
	
	@Test
	@Order(1)
	public void testGetInstance() {
		usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
		assertNotNull(usersResource);
	}

	@Test
	@Order(2)
	public void addUser() {
		UserRepresentation userRepresentation = userServiceInterace.addUser(user);
		assertNotNull(userRepresentation);
		assertEquals(userRepresentation.getFirstName(), user.getFirstName());
		UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
		addedUser = usersResource.search(user.getEmail()).get(0);
	}
	
	
	@AfterClass
	public static void deleteUser() {
		usersResource.get(addedUser.getId()).remove();
	}
	

}
