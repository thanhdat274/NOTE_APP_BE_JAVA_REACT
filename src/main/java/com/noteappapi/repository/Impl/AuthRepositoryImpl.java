package com.noteappapi.repository.Impl;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.AuthRepository;
import com.noteappapi.util.HashUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.DateTimeException;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {
	private final HikariDataSource hikariDataSource;

	public AuthResponse signUp(Users user) {
		log.info("Begin sign up request: {}", user);
		// Check if the email already exists in the database
		Connection connection = null;
		PreparedStatement statement = null;
		CallableStatement callableStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.EMAIL_EXISTS_SQL);
			statement.setString(1, user.getEmail());
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				int count = resultSet.getInt("count");
				log.info("Count: " + count);
				if (count > 0) {
					log.error("Email already exists: {}", user.getEmail());
					return AuthResponse.builder()
							.code("01")
							.message("Email already exists")
							.build();
				}
			}
			// Create a new user if the email does not exist
			callableStatement = connection.prepareCall(SqlConstant.INSERT_USER_SQL);
			callableStatement.setString("P_EMAIL", user.getEmail());
			callableStatement.setString("P_USERNAME", user.getUsername());
			callableStatement.setString("P_PASSWORD", HashUtil.hash256PassWord(user.getPassword()));
			callableStatement.execute();
			log.info("Successfully saved transaction to the database: {}", callableStatement);

			// Get user information after adding the new user
			PreparedStatement getUserStatement = connection.prepareStatement(SqlConstant.GET_USER_SQL);
			getUserStatement.setString(1, user.getEmail());
			ResultSet userResultSet = getUserStatement.executeQuery();
			Users data = new Users();
			if (userResultSet.next()) {
				data.setId(userResultSet.getInt("id"));
				data.setUsername(userResultSet.getString("username"));
				data.setEmail(userResultSet.getString("email"));
			}
			log.info("User data new: {}", data);
			return AuthResponse.builder()
					.code("00")
					.message("Sign-up successful")
					.data(new HashMap<>() {{
						put("user", data);
					}})
					.build();
		} catch (DateTimeException e) {
			log.error("Error while converting date: ", e);
			return AuthResponse.builder()
					.code("02")
					.message("Sign-up failed")
					.build();
		} catch (Exception e) {
			log.error("Error while connecting to the database: ", e);
			return AuthResponse.builder()
					.code("02")
					.message("Sign-up failed")
					.build();
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			try {
				if (null != statement) {
					statement.close();
				}
				if (null != connection) {
					connection.close();
				}
				if (null != callableStatement) {
					callableStatement.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing statement or connection: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to database: ", e);
			}
		}
	}

	public Users logIn(Users userModel) {
		log.info("Attempting to log in with email: {}", userModel.getEmail());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_USER_SQL);
			statement.setString(1, userModel.getEmail());
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				String savedPassword = resultSet.getString("password");
				if (HashUtil.hash256PassWord(userModel.getPassword()).equalsIgnoreCase(savedPassword)) {
					Users user = new Users();
					user.setId(resultSet.getInt("id"));
					user.setUsername(resultSet.getString("username"));
					user.setEmail(resultSet.getString("email"));
					log.info("User logged in successfully with email: {}", userModel.getEmail());
					return user;
				} else {
					log.warn("Incorrect password for email: {}", userModel.getEmail());
					return null;
				}
			} else {
				log.warn("Email not found in the database: {}", userModel.getEmail());
				return null;
			}
		} catch (Exception e) {
			log.error("Error while connecting to the database: ", e);
			return null;
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			try {
				if (null != statement) {
					statement.close();
				}
				if (null != connection) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing statement or connection: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to database: ", e);
			}
		}
	}

}
