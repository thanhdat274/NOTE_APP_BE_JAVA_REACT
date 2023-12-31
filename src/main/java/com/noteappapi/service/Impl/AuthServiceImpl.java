package com.noteappapi.service.Impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.Constant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.AuthRepository;
import com.noteappapi.repository.UserRepo;
import com.noteappapi.service.AuthService;
import com.noteappapi.util.CheckToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	@Value("${app.jwt.secret}")
	private String SECRET_KEY;
	private final CheckToken checkToken;
	private final UserRepo userRepo;
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final AuthRepository authRepository;

	public AuthResponse Signup(Users user) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Data signup request: {}", user);
		return authRepository.signUp(user);
	}

	public AuthResponse Login(Users user) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Processing login for user: {}", user.getEmail());
		Users isAuthenticated = authRepository.logIn(user);
		log.info("User login request: {}", isAuthenticated);
		if (null != isAuthenticated) {
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

	public AuthResponse ForgotPassword(Users users) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Forgot Password for email {}", users.getEmail());
		Users checkEmailUser = authRepository.forgotPassword(users.getEmail());
		log.info("Forgot Password for email {}", checkEmailUser);
		if (null != checkEmailUser) {
			// Calculate the expiration time for the token (e.g., 30 days from now)
			long expirationTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000L;
			Date expirationDate = new Date(expirationTimeMillis);
			log.info("Token expiration date: {}", expirationDate);
			log.info("key: " + SECRET_KEY);
			// Generate the JWT token
			String token = Jwts.builder()
					.setSubject(String.valueOf(checkEmailUser.getId()))
					.setExpiration(expirationDate)
					.signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
					.compact();
			// Tạo đường dẫn đặt lại mật khẩu
			String resetLink = Constant.BaseUrlFE + "/reset-password?token=" + token;
			Context context = new Context();
			context.setVariable("name", checkEmailUser.getUsername());
			context.setVariable("url", resetLink);

			String emailContent = templateEngine.process("forgot-password-email", context);
			log.info("template engine   " + emailContent);

			MimeMessage message = mailSender.createMimeMessage();
			log.info("message " + message);
			try {
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
				helper.setTo(checkEmailUser.getEmail());
				helper.setSubject("Đặt lại mật khẩu mới cho tài khoản " + checkEmailUser.getEmail());

				helper.setText(emailContent, true);
				mailSender.send(message);
				log.info("Gửi mail thành công: " + message);
				return AuthResponse.builder().code("00").message("Gửi email thành công").build();
			} catch (MessagingException e) {
				log.error("Failed sending email", e);
				return AuthResponse.builder().code("02").message("Gửi mail không thành công").build();
			}
		} else {
			return AuthResponse.builder().code("01").message("Tài khoản email không tồn tại").build();
		}
	}

	public AuthResponse ResetPassword(String token, Users users) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("token: " + token);
		log.info("users: " + users);
		try {
			AuthResponse checkTokenResponse = checkToken.tokenResetPassword(token);
			String SUCCESS_CODE = "00";
			if (!SUCCESS_CODE.equals(checkTokenResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return checkTokenResponse;
			}
			Integer authId = (Integer) checkTokenResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return AuthResponse.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}
			return authRepository.resetPassword(checkUser.getEmail(), users);
		} catch (Exception e) {
			log.error("Error while processing DeleteFolder", e);
			return AuthResponse.builder()
					.code("02")
					.message("Lỗi trong quá trình xử lý")
					.build();
		}
	}

}
