package com.noteappapi.repository;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.Users;

public interface AuthRepository {
	AuthResponse signUp(Users user);

	Users logIn(Users user);
}
