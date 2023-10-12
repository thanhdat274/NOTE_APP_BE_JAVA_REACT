package com.noteappapi.repository.Impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.noteappapi.model.Folder;
import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.repository.NoteRepo;
import com.noteappapi.util.DBUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor

public class NoteRepoImpl implements NoteRepo {
	private final HikariDataSource hikariDataSource;

	public List<Note> ListNotes(Integer folderId, Folder folder) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_NOTE_SQL);
			preparedStatement.setInt(1, folderId); // Set the parameter

			resultSet = preparedStatement.executeQuery();

			// Process the result set and return a list of Folder objects
			List<Note> notes = new ArrayList<>();
			while (resultSet.next()) {
				Note note = new Note();
				note.setId(resultSet.getInt("id"));
				note.setName(resultSet.getString("name"));
				note.setContent(resultSet.getString("content"));
				note.setFolderId(folder);
				note.setCreatedAt(resultSet.getTimestamp("created_at"));
				note.setUpdatedAt(resultSet.getTimestamp("updated_at"));
				notes.add(note);
			}
			log.info("list notes: {}", resultSet);
			return notes;
		} catch (SQLException e) {
			log.error("Error while connecting to the database: ", e);
			return null; // Handle the error appropriately
		} finally {
			// Close resources in the finally block
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public Note CreateNote(Folder checkFolder, Note note) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		log.info("Creating folder " + note);
		Connection connection = null;
		CallableStatement callableStatement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			callableStatement = connection.prepareCall(SqlConstant.INSERT_NOTE_SQL);
			callableStatement.setString("P_NAME", note.getName());
			callableStatement.setString("P_CONTENT", note.getContent());
			callableStatement.setInt("P_FOLDER_ID", checkFolder.getId());
			callableStatement.execute();

			preparedStatement = connection.prepareStatement(SqlConstant.GET_NEWLY_CREATED_NOTE_SQL);
			preparedStatement.setInt(1, checkFolder.getId());

			resultSet = preparedStatement.executeQuery();

			Note createdNote = new Note();
			if (resultSet.next()) {
				createdNote.setId(resultSet.getInt("id"));
				createdNote.setName(resultSet.getString("name"));
				createdNote.setContent(resultSet.getString("content"));
				createdNote.setFolderId(checkFolder);
				createdNote.setCreatedAt(resultSet.getTimestamp("created_at"));
				createdNote.setUpdatedAt(resultSet.getTimestamp("updated_at"));
			}
			return createdNote;
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

	public Note findByIdNote(Integer id) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Note note = null;
		try {
			// Thiết lập kết nối đến cơ sở dữ liệu MySQL
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.GET_NOTE_BY_ID_SQL);
			preparedStatement.setInt(1, id);

			// Thực hiện truy vấn và lấy kết quả
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				note = new Note();
				note.setId(resultSet.getInt("id"));
				note.setName(resultSet.getString("name"));
				note.setContent(resultSet.getString("content"));
			}
			return note;
		} catch (SQLException e) {
			log.error("Error while fetching folder details by ID: ", e);
			return null;
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			DBUtil.cleanUp(connection, null, preparedStatement, resultSet);
		}
	}

	public Note updateNote(Note note) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		CallableStatement callableStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			callableStatement = connection.prepareCall(SqlConstant.UPDATE_NOTE_SQL);

			callableStatement.setInt("P_ID", note.getId());
			callableStatement.setString("P_NAME", note.getName());
			callableStatement.setString("P_CONTENT", note.getContent());
			callableStatement.setInt("P_FOLDER_ID", note.getFolderId().getId());
			callableStatement.executeUpdate();
			// Sau khi cập nhật, bạn có thể truy vấn lại folder để lấy thông tin đã cập nhật
			Note updatedNote = findByIdNote(note.getId());
			if (updatedNote != null) {
				log.info("Note updated: {}", updatedNote);
				return updatedNote;
			} else {
				log.error("Failed to retrieve updated note.");
				return null;
			}
		} catch (SQLException e) {
			log.error("Error while updating note: ", e);
			return null;
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			DBUtil.cleanUp(connection, callableStatement, null, null);
		}
	}

	public ResponseData deleteById(Integer id) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareCall(SqlConstant.DELETE_NOTE_SQL);
			preparedStatement.setInt(1, id);

			preparedStatement.executeUpdate();

			// Kiểm tra xem việc xóa đã thành công hay không
			if (findByIdNote(id) == null) {
				return ResponseData.builder()
						.code("00")
						.message("Successfully deleted note with ID: " + id)
						.build();
			} else {
				return ResponseData.builder()
						.code("01")
						.message("Failed to delete note with ID: " + id)
						.build();
			}
		} catch (SQLException e) {
			log.error("Error while deleting note: ", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during note deletion")
					.build();
		} finally {
			// Đảm bảo kết nối và tài nguyên được đóng đúng cách
			DBUtil.cleanUp(connection, null, preparedStatement, null);
		}
	}

	public boolean findByName(String name, Integer folderId) {
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			preparedStatement = connection.prepareStatement(SqlConstant.CHECK_NOTE_BY_NAME_SQL);
			preparedStatement.setString(1, name);
			preparedStatement.setInt(2, folderId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// Tên thư mục đã tồn tại trong cơ sở dữ liệu
				log.info("Đã tồn tại!");
				return true;
			} else {
				log.info("Không tồn tại!");
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
