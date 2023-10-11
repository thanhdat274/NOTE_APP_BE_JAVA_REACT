package com.noteappapi.model;

public class SqlConstant {
	public static final String INSERT_USER_SQL = "{CALL add_user(?, ?, ?)}";
	public static final String UPDATE_PASSWORD_USER_SQL = "{CALL UpdateUserPassword(?, ?)}";
	public static final String EMAIL_EXISTS_SQL = "SELECT COUNT(*) AS count FROM users WHERE email = ?";
	public static final String GET_USER_SQL = "SELECT * FROM users WHERE email = ?";
	public static final String GET_USER_BY_ID_SQL = "SELECT * FROM users WHERE id = ?";

	// phần gọi mysql của folder
	public static final String GET_FOLDER_SQL = "SELECT * FROM folders WHERE auth_id = ?";
	public static final String INSERT_FOLDER_SQL = "{CALL add_folder(?, ?)}";
	public static final String GET_NEWLY_CREATED_FOLDER_SQL = "SELECT * FROM folders WHERE auth_id = ? ORDER BY " +
			"created_at DESC LIMIT 1";
	public static final String GET_FOLDER_BY_ID_SQL = "SELECT * FROM folders WHERE id = ?";
	public static final String CHECK_FOLDER_BY_NAME_SQL = "SELECT * FROM folders WHERE name = ?";
	public static final String UPDATE_FOLDER_SQL = "{CALL update_folder(?, ?, ?)}";
	public static final String DELETE_FOLDER_SQL = "DELETE FROM folders WHERE id = ?";

	// phần mysql của notes
	public static final String GET_NOTE_SQL = "SELECT * FROM notes WHERE folder_id = ?";
	public static final String INSERT_NOTE_SQL = "{CALL CreateNote(?, ?, ?)}";
	public static final String GET_NEWLY_CREATED_NOTE_SQL = "SELECT * FROM notes WHERE folder_id = ? ORDER BY " +
			"created_at DESC LIMIT 1";
	public static final String GET_NOTE_BY_ID_SQL = "SELECT * FROM notes WHERE id = ?";
	public static final String UPDATE_NOTE_SQL = "{CALL update_note(?, ?, ?, ?)}";
	public static final String DELETE_NOTE_SQL = "DELETE FROM notes WHERE id = ?";
	public static final String CHECK_NOTE_BY_NAME_SQL = "SELECT * FROM notes WHERE name = ? AND folder_id = ?";

}
