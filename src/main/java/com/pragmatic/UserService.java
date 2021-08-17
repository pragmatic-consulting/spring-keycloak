package com.pragmatic;

import java.util.Collections;

import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserServiceInterace {

	
	/**
	 * Add user method.
	 * 
	 * This method add a user in keycloak.
	 */
	public UserRepresentation addUser(User user) {
		// getting an instance of the keycloak admin client from a specific realm. (in keycloak config)
		UsersResource usersResource = KeycloakConfig.getInstance().realm(KeycloakConfig.realm).users();
		// create credential representation since the keycloak user doesn't have only a password
		CredentialRepresentation credentialRepresentation = createPasswordCredentials(user.getPassword());

		UserRepresentation kcUser = new UserRepresentation();
		kcUser.setUsername(user.getEmail());
		kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
		kcUser.setFirstName(user.getFirstName());
		kcUser.setLastName(user.getLastName());
		kcUser.setEmail(user.getEmail());
		kcUser.setEnabled(true);
		kcUser.setEmailVerified(false);
		usersResource.create(kcUser);
		
		return kcUser;
	}

	private static CredentialRepresentation createPasswordCredentials(String password) {
		CredentialRepresentation passwordCredentials = new CredentialRepresentation();
		// setting that the password is not temporary. (if it's true that means the user must change the password in the first login).
		passwordCredentials.setTemporary(false);
		passwordCredentials.setType(CredentialRepresentation.PASSWORD);
		passwordCredentials.setValue(password);
		return passwordCredentials;
	}

}
