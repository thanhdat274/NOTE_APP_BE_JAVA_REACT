package com.noteappapi.service.Impl;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.Users;
import com.noteappapi.repository.FolderRepository;
import com.noteappapi.repository.UserRepo;
import com.noteappapi.service.FolderService;
import com.noteappapi.util.CheckAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {
	private final CheckAuth checkAuth;
	private final FolderRepository folderRepository;
	private final UserRepo userRepo;
	private final String SUCCESS_CODE = "00";

	public ResponseData ListFolder(Integer authId, String token) {
		log.info("Begin checking list folders with authId: {} and token: {}", authId, token);
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			// Check the validity of the token
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", authId);
				return tokenValidationResponse;
			}

			log.info("Auth ID sent in the request: {}", authId);
			// Valid token, check user ID
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}

			// Valid user ID, continue processing
			List<Folder> folders = folderRepository.findByAuthId(authId, checkUser);
			log.info("Successfully retrieved list of folders: {}", folders.toString());
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully retrieved list of folders")
					.data(new HashMap<>() {{
						put("folder", folders);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing listFolder", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during processing")
					.build();
		}
	}

	public ResponseData GetByIdFolders(Integer id, String token) {
		log.info("Begin checking folder with id: {} and token: {}", id, token);
		try {
			// Check the validity of the token
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			log.info("Checking the validity of the token: {}", tokenValidationResponse);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", id);
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
			log.info("Auth ID sent in the request: {}", id);
			Folder folder = folderRepository.findByIdFolder(id, checkUser);
			if (null == folder) {
				return ResponseData.builder()
						.code("01")
						.message("Thư mục không tồn tại!")
						.build();
			}

			log.info("Successfully retrieved folder: {}", folder);
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully retrieved folder")
					.data(new HashMap<>() {{
						put("folder", folder);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing GetByIdFolders", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during processing")
					.build();
		}
	}

	public ResponseData CreateFolder(Integer authId, String token, Folder folder) {
		log.info("Creating folder with id: {} and token: {} and folder request: {}", authId, token, folder);
		try {
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", authId);
				return tokenValidationResponse;
			}

			log.info("Auth ID sent in the request: {}", authId);
			// Valid token, check user ID
			Users checkUser = userRepo.findByUserId(authId);
			if (null == checkUser) {
				log.error("Invalid user ID: {}", authId);
				return ResponseData.builder()
						.code("01")
						.message("Invalid user ID")
						.build();
			}

			if (folderRepository.findByName(folder.getName())) {
				return ResponseData.builder()
						.code("01")
						.message("Tên thư mục đã tồn tại!")
						.build();
			}
			Folder dataFolder = folderRepository.createFolder(folder, checkUser);
			log.info("Folder created: {}", dataFolder);

			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Successfully created folder")
					.data(new HashMap<>() {{
						put("folder", dataFolder);
					}})
					.build();
		} catch (Exception e) {
			log.error("Error while processing CreateFolder", e);
			return ResponseData.builder()
					.code("02")
					.message("Error during processing")
					.build();
		}
	}

	public ResponseData UpdateFolders(Integer id, String token, Folder folder) {
		log.info("Updating folder with id: {} and token: {} and folder request: {}", id, token, folder);
		try {
			// Check the validity of the token
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", id);
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
			log.info("Auth ID sent in the request: {}", id);
			Folder folders = folderRepository.findByIdFolder(id, checkUser);
			if (null == folders) {
				return ResponseData.builder()
						.code("01")
						.message("Thư mục không tồn tại!")
						.build();
			}

			Folder dataFolder = folderRepository.updateFolder(folder, checkUser);

			log.info("Folder updated: {}", dataFolder);
			return ResponseData.builder()
					.code(SUCCESS_CODE)
					.message("Cập nhật thành công")
					.data(new HashMap<>() {{
						put("folder", dataFolder);
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

	public ResponseData DeleteFolder(Integer id, String token) {
		log.info("Deleting folder with id: {} and token: {}", id, token);
		try {
			// Check the validity of the token
			ResponseData tokenValidationResponse = checkAuth.isValidToken(token);
			if (!SUCCESS_CODE.equals(tokenValidationResponse.getCode())) {
				log.error("Token is invalid: {}", id);
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
			log.info("Auth ID sent in the request: {}", id);
			Folder folders = folderRepository.findByIdFolder(id, checkUser);
			if (null == folders) {
				return ResponseData.builder()
						.code("01")
						.message("Thư mục không tồn tại!")
						.build();
			}

			ResponseData folderList = folderRepository.deleteById(id);
			log.info("Folder deleted with id: {}", id);
			return folderList;
		} catch (Exception e) {
			log.error("Error while processing DeleteFolder", e);
			return ResponseData.builder()
					.code("02")
					.message("Lỗi trong quá trình xử lý")
					.build();
		}
	}
}
