package com.noteappapi.repository.Impl;

import com.noteappapi.model.Folder;
import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.SqlConstant;
import com.noteappapi.repository.NoteRepo;
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

public class NoteRepoImpl implements NoteRepo {
	private final HikariDataSource hikariDataSource;

	public List<Note> ListNotes(Integer folderId, Folder folder) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_NOTE_SQL);
			statement.setInt(1, folderId); // Set the parameter

			resultSet = statement.executeQuery();

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

	public Note CreateNote(Folder checkFolder, Note note) {
		log.info("Creating folder " + note);
		Connection connection = null;
		CallableStatement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareCall(SqlConstant.INSERT_NOTE_SQL);
			statement.setString("P_NAME", note.getName());
			statement.setString("P_CONTENT", note.getContent());
			statement.setInt("P_FOLDER_ID", checkFolder.getId());
			statement.execute();

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

	public Note findByIdNote(Integer id) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Note note = null;
		try {
			// Thiết lập kết nối đến cơ sở dữ liệu MySQL
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.GET_NOTE_BY_ID_SQL);
			statement.setInt(1, id);

			// Thực hiện truy vấn và lấy kết quả
			resultSet = statement.executeQuery();

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
				log.error("Error while closing statement or connection: ", e);
			} catch (Exception e) {
				log.error("Error while cleaning connection to database: ", e);
			}
		}
	}

	public Note updateNote(Note note) {
		Connection connection = null;
		CallableStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareCall(SqlConstant.UPDATE_NOTE_SQL);

			statement.setInt("P_ID", note.getId());
			statement.setString("P_NAME", note.getName());
			statement.setString("P_CONTENT", note.getContent());
			statement.setInt("P_FOLDER_ID", note.getFolderId().getId());
			statement.executeUpdate();
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
			try {
				if (statement != null) {
					statement.close();
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

	public ResponseData deleteById(Integer id) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareCall(SqlConstant.DELETE_NOTE_SQL);
			statement.setInt(1, id);

			statement.executeUpdate();

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
			try {
				if (statement != null) {
					statement.close();
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

	public boolean findByName(String name, Integer folderId) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(SqlConstant.CHECK_NOTE_BY_NAME_SQL);
			statement.setString(1, name);
			statement.setInt(2, folderId);

			resultSet = statement.executeQuery();
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
