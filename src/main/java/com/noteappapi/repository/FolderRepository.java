package com.noteappapi.repository;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;
import com.noteappapi.model.Users;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository {
	// Tìm tất cả thư mục của một người dùng dựa trên user ID
	List<Folder> findByAuthId(Integer authId, Users users);

	Folder createFolder(Folder folder, Users checkUser);

	Folder findByIdFolder(Integer id, Users checkUser);

	Folder updateFolder(Folder folder, Users checkUser);

	ResponseData deleteById(Integer id);

	boolean findByName(String name);
}
