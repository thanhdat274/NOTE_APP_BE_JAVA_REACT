package com.noteappapi.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DBUtil {
	public static void cleanUp(Connection connection, CallableStatement callableStatement, PreparedStatement preparedStatement,
	                           ResultSet resultSet) {
		try {
			if (null != callableStatement) {
				callableStatement.close();
			}
			if (null != preparedStatement) {
				preparedStatement.close();
			}
			if (null != connection) {
				connection.close();
			}
			if (null != resultSet) {
				resultSet.close();
			}
		} catch (SQLException e) {
			log.error("Error while closing statement or connection: ", e);
		} catch (Exception e) {
			log.error("Error while cleaning connection to database: ", e);
		}
	}
}
