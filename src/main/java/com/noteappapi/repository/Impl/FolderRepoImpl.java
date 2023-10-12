package com.noteappapi.repository.Impl;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.model.Users;
import com.noteappapi.repository.FolderRepository;
import com.noteappapi.util.DBUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.slf4j.MDC;
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
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("List folder by authId: " + authId);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_FOLDER_SQL);
			preparedStatement.setInt(1, authId); // Set the parameter

			resultSet = preparedStatement.executeQuery();

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
			return null;
		} finally {
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public Folder createFolder(Folder folder, Users checkUser) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Creating folder " + folder);
		Connection connection = null;
		CallableStatement callableStatement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			callableStatement = connection.prepareCall(SqlConstant.INSERT_FOLDER_SQL);
			callableStatement.setString("P_NAME", folder.getName());
			callableStatement.setInt("P_AUTH_ID", checkUser.getId());
			callableStatement.execute();

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
			DBUtil.cleanUp(connection, callableStatement, preparedStatement, resultSet);
		}

	}

	public Folder findByIdFolder(Integer id, Users checkUser) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("findByIdFolder called with id " + id);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_FOLDER_BY_ID_SQL);
			preparedStatement.setInt(1, id); // Đặt tham số ID
			resultSet = preparedStatement.executeQuery();
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
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public Folder updateFolder(Folder folder, Users checkUser) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Updating folder with ID: {}", folder.getId());
		Connection connection = null;
		CallableStatement callableStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			callableStatement = connection.prepareCall(SqlConstant.UPDATE_FOLDER_SQL);
			callableStatement.setInt("P_ID", folder.getId());
			callableStatement.setString("P_NAME", folder.getName());
			callableStatement.setInt("P_AUTH_ID", folder.getAuthId().getId());
			callableStatement.execute();

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
			DBUtil.cleanUp(connection, callableStatement, null, null);
		}
	}

	public ResponseData deleteById(Integer id) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
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
			DBUtil.cleanUp(connection, null, preparedStatement, null);
		}
	}

	public boolean findByName(String name) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.CHECK_FOLDER_BY_NAME_SQL);
			preparedStatement.setString(1, name);

			resultSet = preparedStatement.executeQuery();
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
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

}
