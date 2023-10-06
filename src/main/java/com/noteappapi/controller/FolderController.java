package com.noteappapi.controller;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;
import com.noteappapi.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {
	private final FolderService folderService;

	@ExceptionHandler  // đây là hàm xử lý khi trong postman ko có dữ liệu vẫn bấm gửi
	public ResponseEntity<ResponseData> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		log.info("Request data not found: ", ex);
		ResponseData responseData = ResponseData.builder().code("99")
				.message("Data not found!")
				.build();
		return ResponseEntity.badRequest().body(responseData);
	}

	@GetMapping("/{authId}")
	public ResponseEntity<ResponseData> listFolder(@PathVariable("authId") Integer authId,
	                                               @RequestHeader("Authorization") String token) {
		ResponseData responseData = folderService.ListFolder(authId, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@GetMapping("/detail/{id}")
	public ResponseEntity<ResponseData> getByIdFolder(@PathVariable("id") Integer id,
	                                                  @RequestHeader("Authorization") String token) {
		ResponseData responseData = folderService.GetByIdFolders(id, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("/{authId}")
	public ResponseEntity<ResponseData> addFolder(@PathVariable("authId") Integer authId, @RequestHeader(
			"Authorization") String token, @RequestBody Folder folder) {
		ResponseData responseData = folderService.CreateFolder(authId, token, folder);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData> updateFolder(@PathVariable("id") Integer id,
	                                                 @RequestHeader("Authorization") String token,
	                                                 @RequestBody Folder folder) {
		ResponseData responseData = folderService.UpdateFolders(id, token, folder);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData> deleteFolder(@PathVariable("id") Integer id,
	                                      @RequestHeader("Authorization") String token) {
		ResponseData responseData = folderService.DeleteFolder(id, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
}
