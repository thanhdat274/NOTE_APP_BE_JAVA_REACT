package com.noteappapi.controller;

import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;
import com.noteappapi.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {
	private final NoteService noteService;

	@ExceptionHandler  // đây là hàm xử lý khi trong postman ko có dữ liệu vẫn bấm gửi
	public ResponseEntity<ResponseData> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		log.info("Request data not found: ", ex);
		ResponseData responseData = ResponseData.builder().code("99")
				.message("Data not found!")
				.build();
		return ResponseEntity.badRequest().body(responseData);
	}

	@GetMapping("/{folderId}")
	public ResponseEntity<ResponseData> listNotes(@PathVariable("folderId") Integer folderId,
	                                              @RequestHeader("Authorization") String token) {
		ResponseData responseData = noteService.getNotes(folderId, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseData> createNote(@RequestHeader(
			"Authorization") String token, @RequestBody Note note) {
		ResponseData responseData = noteService.addNote(token, note);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@GetMapping("/detail/{id}")
	public ResponseEntity<ResponseData> getByIdNote(@PathVariable("id") Integer id, @RequestHeader(
			"Authorization") String token) {
		ResponseData responseData = noteService.GetByIdNotes(id, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData> updateNote(@PathVariable("id") Integer id,
	                                               @RequestHeader("Authorization") String token,
	                                               @RequestBody Note updatedNote) {
		ResponseData responseData = noteService.updateNoteById(id, token, updatedNote);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData> deleteNote(@PathVariable("id") Integer id,
	                                               @RequestHeader("Authorization") String token) {
		ResponseData responseData = noteService.deleteNoteById(id, token);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

}
