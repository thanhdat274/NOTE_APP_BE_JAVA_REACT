package com.noteappapi.controller;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.Users;
import com.noteappapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@ExceptionHandler  // đây là hàm xử lý khi trong postman ko có dữ liệu vẫn bấm gửi
	public ResponseEntity<ResponseData> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		log.info("Request data not found: ", ex);
		ResponseData responseData = ResponseData.builder().code("99")
				.message("Data not found!")
				.build();
		return ResponseEntity.badRequest().body(responseData);
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody Users users) {
		AuthResponse responseData = authService.Signup(users);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody Users user) {
		AuthResponse responseData = authService.Login(user);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("/forgot_password")
	public ResponseEntity<AuthResponse> forgotPassword(@RequestBody Users users) {
		// Xử lý quên mật khẩu và gửi email với mã token JWT
		AuthResponse responseData = authService.ForgotPassword(users);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("/reset_password")
	public ResponseEntity<AuthResponse> resetPassword(@RequestParam String token, @RequestBody Users users) {
		AuthResponse responseData = authService.ResetPassword(token, users);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
}
