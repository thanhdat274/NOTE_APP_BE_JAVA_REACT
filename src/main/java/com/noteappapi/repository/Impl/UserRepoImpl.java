package com.noteappapi.repository.Impl;

import com.noteappapi.model.SqlConstant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.UserRepo;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepoImpl implements UserRepo {
	private final HikariDataSource hikariDataSource;

	public Users findByUserId(Integer id) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_USER_BY_ID_SQL);
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			Users user = new Users();
			if (resultSet.next()) {
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setEmail(resultSet.getString("email"));
				user.setRole(resultSet.getInt("role"));
				user.setCreatedAt(resultSet.getTimestamp("created_at"));
				user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
			}
			return user;
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