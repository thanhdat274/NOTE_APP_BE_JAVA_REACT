package com.noteappapi.service;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.Users;

public interface AuthService {
	AuthResponse Signup(Users users);

	AuthResponse Login(Users users);

	AuthResponse ForgotPassword(Users users);

	AuthResponse ResetPassword(String token, Users users);
}
