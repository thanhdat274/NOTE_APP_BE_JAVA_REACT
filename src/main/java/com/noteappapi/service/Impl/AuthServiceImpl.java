package com.noteappapi.service.Impl;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.Users;
import com.noteappapi.repository.AuthRepository;
import com.noteappapi.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final AuthRepository authRepository;

	@Value("${app.jwt.secret}")
	private String SECRET_KEY;

	public AuthResponse Signup(@RequestBody Users user) {
		log.info("Data signup request: {}", user);
		return authRepository.signUp(user);
	}

	public AuthResponse Login(@RequestBody Users user) {
		log.info("Processing login for user: {}", user.getEmail());
		Users isAuthenticated = authRepository.logIn(user);
		log.info("User login request: {}", isAuthenticated);
		if (isAuthenticated != null) {
			log.info("Email {} authenticated successfully.", user.getEmail());

			// Calculate the expiration time for the token (e.g., 30 days from now)
			long expirationTimeMillis = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L;
			Date expirationDate = new Date(expirationTimeMillis);
			log.info("Token expiration date: {}", expirationDate);
			log.info("key: " + SECRET_KEY);
			// Generate the JWT token
			String token = Jwts.builder()
					.setSubject(String.valueOf(isAuthenticated.getId()))
					.setExpiration(expirationDate)
					.signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
					.compact();

			log.info("Token created: {} for user: {}", token, user.getEmail());

			return AuthResponse.builder()
					.code("00").message("Login successful")
					.data(new HashMap<>() {{
						put("user", isAuthenticated);
						put("token", token);
					}})
					.build();
		} else {
			log.error("Authentication failed for email {}.", user.getEmail());
			return AuthResponse.builder()
					.code("01")
					.message("Invalid email or password")
					.build();
		}
	}
}
