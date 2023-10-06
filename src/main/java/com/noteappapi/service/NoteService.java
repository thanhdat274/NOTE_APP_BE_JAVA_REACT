package com.noteappapi.service;

import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;

public interface NoteService {
	ResponseData getNotes(Integer folderId, String token);

	ResponseData addNote(String token, Note note);

	ResponseData GetByIdNotes(Integer id, String token);

	ResponseData updateNoteById(Integer id, String token, Note updatedNote);

	ResponseData deleteNoteById(Integer id, String token);
}
