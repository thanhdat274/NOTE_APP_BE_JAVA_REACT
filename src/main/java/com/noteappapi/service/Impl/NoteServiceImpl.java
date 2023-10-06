package com.noteappapi.service.Impl;

import com.noteappapi.model.Folder;
import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.Users;
import com.noteappapi.repository.FolderRepository;
import com.noteappapi.repository.NoteRepo;
import com.noteappapi.repository.UserRepo;
import com.noteappapi.service.NoteService;
import com.noteappapi.util.CheckAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
	private final CheckAuth checkAuth;
	private final UserRepo userRepo;
	private final NoteRepo noteRepo;
	private final FolderRepository folderRepository;
	private final String SUCCESS_CODE = "00";

	public ResponseData getNotes(Integer folderId, String token) {
		log.info("Start checking folder id: {} and token data user: {}", folderId, token);
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return tokenValidationResponse;
			}

			log.info("Checking the validity of the token is successful : {}", tokenValidationResponse);
			Integer authId = (Integer) tokenValidationResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}

			Folder checkFolder = folderRepository.findByIdFolder(folderId, checkUser);
			if (null == checkFolder) {
				return ResponseData.builder()
						.code("01")
						.message("Thư mục không tồn tại!")
						.build();
			}
			log.info("Checking folder " + checkFolder);

			List<Note> listNotes = noteRepo.ListNotes(folderId, checkFolder);
			log.info("Successfully retrieved list of folders: {}", listNotes);
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully retrieved list of notes")
					.data(new HashMap<>() {{
						put("notes", listNotes);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing listNotes", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during processing")
					.build();
		}
	}

	public ResponseData addNote(String token, Note note) {
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return tokenValidationResponse;
			}

			log.info("Checking the validity of the token is successful : {}", tokenValidationResponse);
			Integer authId = (Integer) tokenValidationResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}
			Integer folder = note.getFolderId().getId();
			log.info("Folder ID is {}", folder);
			Folder checkFolder = folderRepository.findByIdFolder(folder, checkUser);
			if (null == checkFolder) {
				return ResponseData.builder()
						.code("01")
						.message("Thư mục không tồn tại!")
						.build();
			}

			if (noteRepo.findByName(note.getName(), note.getFolderId().getId() )) {
				return ResponseData.builder()
						.code("01")
						.message("Tên note đã tồn tại!")
						.build();
			}
			Note dataNotes = noteRepo.CreateNote(checkFolder, note);
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully created notes")
					.data(new HashMap<>() {{
						put("folder", dataNotes);
					}})
					.build();
		} catch (Exception e) {

			return null;
		}
	}

	public ResponseData GetByIdNotes(Integer id, String token) {
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return tokenValidationResponse;
			}

			log.info("Checking the validity of the token is successful : {}", tokenValidationResponse);
			Integer authId = (Integer) tokenValidationResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}
			Note note = noteRepo.findByIdNote(id);
			if (note == null) {
				return ResponseData.builder()
						.code("01")
						.message("Note không tồn tại!")
						.build();
			}

			log.info("Successfully retrieved details of note: {}", note);

			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully retrieved note details")
					.data(new HashMap<>() {{
						put("note", note);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing GetByIdNotes", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during processing")
					.build();
		}
	}

	public ResponseData updateNoteById(Integer id, String token, Note updatedNote) {
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return tokenValidationResponse;
			}

			log.info("Checking the validity of the token is successful : {}", tokenValidationResponse);
			Integer authId = (Integer) tokenValidationResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}
			// Lấy thông tin note cần cập nhật dựa trên id
			Note existingNote = noteRepo.findByIdNote(id);
			if (null == existingNote) {
				return ResponseData.builder()
						.code("01")
						.message("Note không tồn tại!")
						.build();
			}
			// Kiểm tra xem người dùng có quyền cập nhật note không
			Note dataNote = noteRepo.updateNote(updatedNote);

			log.info("Folder updated: {}", dataNote);
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Cập nhật thành công")
					.data(new HashMap<>() {{
						put("notes", dataNote);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing UpdateFolders", e);
			return ResponseData.builder()
					.code("02")
					.message("Lỗi trong quá trình xử lý")
					.build();
		}
	}

	public ResponseData deleteNoteById(Integer id, String token) {
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", token);
				return tokenValidationResponse;
			}

			log.info("Checking the validity of the token is successful : {}", tokenValidationResponse);
			Integer authId = (Integer) tokenValidationResponse.getData().get("authId");
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}
			// Lấy thông tin note cần xóa dựa trên id
			Note existingNote = noteRepo.findByIdNote(id);
			if (null == existingNote) {
				return ResponseData.builder()
						.code("01")
						.message("Note không tồn tại!")
						.build();
			}
			ResponseData deleteResponse = noteRepo.deleteById(id);
			log.info("Folder deleted with id: {}", id);
			return deleteResponse;
		} catch (Exception e) {
			log.error("Error while processing deleteNoteById", e);
			return ResponseData.builder()
					.code("02")
					.message("Lỗi trong quá trình xử lý")
					.build();
		}
	}
}
