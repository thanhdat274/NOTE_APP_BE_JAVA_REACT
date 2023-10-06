package com.noteappapi.repository;

import com.noteappapi.model.Folder;
import com.noteappapi.model.Note;
import com.noteappapi.model.ResponseData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepo {
	List<Note> ListNotes(Integer folderId, Folder folder);

	Note CreateNote(Folder folder, Note note);

	Note findByIdNote(Integer id);

	Note updateNote(Note updatedNote);

	ResponseData deleteById(Integer id);

	boolean findByName(String name, Integer folderId);
}
