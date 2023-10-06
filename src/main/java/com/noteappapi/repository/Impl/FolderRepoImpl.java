package com.noteappapi.repository.Impl;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.FolderRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FolderRepoImpl implements FolderRepository {
	private final HikariDataSource hikariDataSource;

	public List<Folder> findByAuthId(Integer authId, Users users) {
		log.info("List folder by authId: " + authId);
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_FOLDER_SQL);
			statement.setInt(1, authId); // Set the parameter

			resultSet = statement.executeQuery();

			// Process the result set and return a list of Folder objects
			List<Folder> folders = new ArrayList<>();
			while (resultSet.next()) {
				Folder folder = new Folder();
				folder.setId(resultSet.getInt("id"));
				folder.setName(resultSet.getString("name"));
				folder.setAuthId(users);
				folder.setCreatedAt(resultSet.getTimestamp("created_at"));
				folder.setUpdatedAt(resultSet.getTimestamp("updated_at"));
				folders.add(folder);
			}
//			log.info("list folders: {}", folders);
			return folders;
		} catch (SQLException e) {
			log.error("Error while connecting to the database: ", e);
			return null; // Handle the error appropriately
		} finally {
			// Close resources in the finally block
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing resources: ", e);
			}
		}
	}

	public Folder createFolder(Folder folder, Users checkUser) {
		log.info("Creating folder " + folder);
		Connection connection = null;
		CallableStatement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareCall(SqlConstant.INSERT_FOLDER_SQL);
			statement.setString("P_NAME", folder.getName());
			statement.setInt("P_AUTH_ID", checkUser.getId());
			statement.execute();

			preparedStatement = connection.prepareStatement(SqlConstant.GET_NEWLY_CREATED_FOLDER_SQL);
			preparedStatement.setInt(1, checkUser.getId());

			resultSet = preparedStatement.executeQuery();

			Folder createdFolder = new Folder();
			if (resultSet.next()) {
				createdFolder.setId(resultSet.getInt("id"));
				createdFolder.setName(resultSet.getString("name"));
				createdFolder.setAuthId(checkUser);
				createdFolder.setCreatedAt(resultSet.getTimestamp("created_at"));
				createdFolder.setUpdatedAt(resultSet.getTimestamp("updated_at"));
			}
			return createdFolder;
		} catch (DateTimeException e) {
			log.error("Error while converting date: ", e);
			return null;
		} catch (Exception e) {
			log.error("Error while connecting to the database: ", e);
			return null;
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing resources: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to database: ", e);
			}
		}

	}

	public Folder findByIdFolder(Integer id, Users checkUser) {
		log.info("findByIdFolder called with id " + id);
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_FOLDER_BY_ID_SQL);
			statement.setInt(1, id); // Đặt tham số ID
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				Folder folder = new Folder();
				folder.setId(resultSet.getInt("id"));
				folder.setName(resultSet.getString("name"));
				folder.setAuthId(checkUser);
				folder.setCreatedAt(resultSet.getTimestamp("created_at"));
				folder.setUpdatedAt(resultSet.getTimestamp("updated_at"));
				log.info("details folder theo id: " + folder);
				return folder;
			} else {
				// resultSet rỗng, không tìm thấy thư mục
				log.info("Không tìm thấy thư mục với id: " + id);
				return null;
			}
		} catch (Exception e) {
			log.error("Error while fetching folder details by ID: ", e);
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

	public Folder updateFolder(Folder folder, Users checkUser) {
		log.info("Updating folder with ID: {}", folder.getId());
		Connection connection = null;
		CallableStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareCall(SqlConstant.UPDATE_FOLDER_SQL);
			statement.setInt("P_ID", folder.getId());
			statement.setString("P_NAME", folder.getName());
			statement.setInt("P_AUTH_ID", folder.getAuthId().getId());
			statement.execute();

			// Sau khi cập nhật, bạn có thể truy vấn lại folder để lấy thông tin đã cập nhật
			Folder updatedFolder = findByIdFolder(folder.getId(), checkUser);
			if (updatedFolder != null) {
				log.info("Folder updated: {}", updatedFolder);
				return updatedFolder;
			} else {
				log.error("Failed to retrieve updated folder.");
				return null;
			}
		} catch (SQLException e) {
			log.error("Error while updating folder: ", e);
			return null;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing resources: ", e);
			}
		}
	}

	public ResponseData deleteById(Integer id) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.DELETE_FOLDER_SQL);
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
			return ResponseData.builder()
					.code("00")
					.message("Xóa thành công")
					.build();
		} catch (Exception e) {
			log.error("Error while deleting folder: ", e);
			return ResponseData.builder()
					.code("00")
					.message("Error while deleting folder")
					.build();
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing statement or connection: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to the database: ", e);
			}
		}
	}

	public boolean findByName(String name) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.CHECK_FOLDER_BY_NAME_SQL);
			statement.setString(1, name);

			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				// Tên thư mục đã tồn tại trong cơ sở dữ liệu
				log.info("Tên thư mục đã tồn tại!");
				return true;
			} else {
				log.info("Tên thư mục không có trong DB!");
				// Tên thư mục chưa tồn tại trong cơ sở dữ liệu
				return false;
			}
		} catch (SQLException e) {
			log.error("Error while checking folder by name: ", e);
			return false;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Error while closing resources: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to the database: ", e);
			}
		}
	}

}
