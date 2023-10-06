package com.noteappapi.repository;

import com.noteappapi.model.Users;

public interface UserRepo {
	// Tìm tất cả thư mục của một người dùng dựa trên user ID
	Users findByUserId(Integer id);
}
