package com.pragmatic;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.RefreshToken;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Controller
public class HelloController {

	private final String CLIENT_ID = "second-application";
	private final int FORBBIDEN_CODE = 401;
	private final byte ALLOWED_ATTEMPTS = 2;

	@Autowired
	private UserServiceInterace userServiceInterace;
	
//    @Autowired
//    private KeycloakRestTemplate keycloakRestTemplate;

	@GetMapping("/manager")
	@ResponseBody
	public String helloManager() {
		return "If you're seeing this you have role : MANAGER, from the first app";
	}
	
	@PostMapping("/add-user")
	@ResponseBody
	public UserRepresentation addUser(@RequestBody User user){
		return this.userServiceInterace.addUser(user);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/admin", produces = "application/json;charset=utf-8")
	@ResponseBody
	public String helloAdmin(final HttpServletRequest request) throws InterruptedException {
		String offline_token = request.getHeader("offline_token");
		System.out.println("thread sleeping for 1 min");
		Thread.sleep(60 * 1000 + 5);
		System.out.println("sending request...");
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = sendRequestToFirstApp(request, false, null);
		} catch (HttpClientErrorException exception) {
			System.out.println("HTTP Server Exception : Token invalid!");
			ResponseEntity<AccessTokenResponse> tokenResponseEntity = this.refreshToken(offline_token);
			System.out.println("token refreshed!");
			System.out.println("resending request...");
			AccessTokenResponse accessToken = tokenResponseEntity.getBody();
			responseEntity = sendRequestToFirstApp(request, true, accessToken.getToken());
		}
		if (responseEntity == null)
			return null;

		String response = responseEntity.getBody();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/test-micro", produces = "application/json;charset=utf-8")
	@ResponseBody
	public String helloMicro(final HttpServletRequest request) throws InterruptedException {
		return "if you're seeing this you're calling this from another microservice";
	}

	
	private ResponseEntity<AccessTokenResponse> refreshToken(String offlineToken) {
		System.out.println("refreshing token!");
		int attempt = 1;
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("client_id", CLIENT_ID);
		map.add("grant_type", "refresh_token");
		map.add("refresh_token", offlineToken);
		map.add("client_secret", "9279652e-3bb3-41a8-ab7c-8253f3ed9437");
		HttpEntity entity = new HttpEntity(map, httpHeaders);
		ResponseEntity<AccessTokenResponse> responseEntity = null;
		while (attempt <= this.ALLOWED_ATTEMPTS) {
			System.out.println("Attempt : " + attempt);
			responseEntity = restTemplate.exchange(
					"http://localhost:8080/auth/realms/poc-realm/protocol/openid-connect/token", HttpMethod.POST,
					entity, AccessTokenResponse.class);
			if (responseEntity.getStatusCodeValue() == FORBBIDEN_CODE)
				attempt++;
			else
				break;
		}
		return responseEntity;
	}

	private ResponseEntity<String> sendRequestToFirstApp(HttpServletRequest request, boolean refresh,
			String accessToken) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
		KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
		KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		if (!refresh) {
			httpHeaders.add("Authorization", "Bearer " + keycloakSecurityContext.getTokenString());
		} else {
			httpHeaders.add("Authorization", "Bearer " + accessToken);
		}
		httpHeaders.add("offline_token", request.getHeader("offline_token"));
		HttpEntity httpEntity = new HttpEntity(httpHeaders);
		ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8082/second-app/admin",
				HttpMethod.GET, httpEntity, String.class);
		return responseEntity;
	}
}
