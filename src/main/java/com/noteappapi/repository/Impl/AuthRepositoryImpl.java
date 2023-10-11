package com.noteappapi.repository.Impl;

import com.noteappapi.model.AuthResponse;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.AuthRepository;
import com.noteappapi.util.DBUtil;
import com.noteappapi.util.HashUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {
	private final HikariDataSource hikariDataSource;

	public AuthResponse signUp(Users user) {
		log.info("Starting sign-up process for user: {}", user);

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.EMAIL_EXISTS_SQL);
			preparedStatement.setString(1, user.getEmail());
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				int count = resultSet.getInt("count");
				log.info("User count: " + count);
				if (count > 0) {
					log.error("Email already exists: {}", user.getEmail());
					return AuthResponse.builder()
							.code("01")
							.message("Email already exists")
							.build();
				}
			}

			callableStatement = connection.prepareCall(SqlConstant.INSERT_USER_SQL);
			callableStatement.setString("P_EMAIL", user.getEmail());
			callableStatement.setString("P_USERNAME", user.getUsername());
			callableStatement.setString("P_PASSWORD", HashUtil.hash256PassWord(user.getPassword()));
			callableStatement.execute();
			log.info("Successfully saved user data to the database: {}", callableStatement);

			PreparedStatement getUserStatement = connection.prepareStatement(SqlConstant.GET_USER_SQL);
			getUserStatement.setString(1, user.getEmail());
			ResultSet userResultSet = getUserStatement.executeQuery();
			Users data = new Users();
			if (userResultSet.next()) {
				data.setId(userResultSet.getInt("id"));
				data.setUsername(userResultSet.getString("username"));
				data.setEmail(userResultSet.getString("email"));
			}
			log.info("New user data: {}", data);
			return AuthResponse.builder()
					.code("00")
					.message("Sign-up successful")
					.data(new HashMap<>() {{
						put("user", data);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while connecting to the database: ", e);
			return AuthResponse.builder()
					.code("02")
					.message("Sign-up failed")
					.build();
		} finally {
			DBUtil.cleanUp(connection, callableStatement, preparedStatement, resultSet);
		}
	}

	public Users logIn(Users userModel) {
		log.info("Attempting to log in with email: {}", userModel.getEmail());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_USER_SQL);
			preparedStatement.setString(1, userModel.getEmail());
			resultSet = preparedStatement.executeQuery();

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
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public Users forgotPassword(String email) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_USER_SQL);
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			Users users = new Users();
			if (resultSet.next()) {
				users.setId(resultSet.getInt("id"));
				users.setEmail(resultSet.getString("email"));
				users.setUsername(resultSet.getString("username"));
				log.info("Users " + users);
				return users;
			} else {
				log.warn("Email not found in the database: {}", email);
				return null;
			}
		} catch (Exception e) {
			log.error("Exception while getting users information ", e);
			return null;
		} finally {
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public AuthResponse resetPassword(String email, Users users) {
		Connection connection = null;
		CallableStatement callableStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			callableStatement = connection.prepareCall(SqlConstant.UPDATE_PASSWORD_USER_SQL);
			callableStatement.setString("P_EMAIL", email);
			callableStatement.setString("P_NEWPASSWORD", HashUtil.hash256PassWord(users.getPassword()));
			callableStatement.executeUpdate();

			return AuthResponse.builder()
					.code("00")
					.message("Password change successful!")
					.build();
		} catch (Exception e) {
			log.error("Error while connecting to the database: ", e);
			return AuthResponse.builder()
					.code("02")
					.message("Password change failed!")
					.build();
		} finally {
			DBUtil.cleanUp(connection, callableStatement, null, null);
		}
	}
}

