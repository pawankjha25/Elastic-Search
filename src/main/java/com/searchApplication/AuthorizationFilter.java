package com.searchApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AuthorizationFilter implements Filter {

	public static String authVerifyURL = "http://74.63.228.198:9090/api/Resource/ValidateToken";
	private Environment env;
	private RestTemplate restTemplate;

	public AuthorizationFilter(Environment env) {
		this.setEnv(env);
		authVerifyURL = env.getProperty("auth_token_verify");
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (env.getProperty("zDaly.bypassSecurity") != null && !Boolean.valueOf(env.getProperty("zDaly.bypassSecurity"))) {
			String user_token = ((HttpServletRequest) request).getHeader("user_token");
			if (user_token == null || user_token.isEmpty()) {
				throw new IOException("Invalid/Empty user token");
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + user_token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
			ResponseEntity<AuthResponse> responseAuth = restTemplate.exchange(authVerifyURL, HttpMethod.GET, requestEntity, AuthResponse.class);
			AuthResponse authResponse = responseAuth.getBody();
			if (authResponse.getResponseCode() == HttpStatus.UNAUTHORIZED.toString()) {
				throw new ServletException("Unauthorized Request");
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		restTemplate = new RestTemplate();
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

}
