package com.noteappapi.service;

import com.noteappapi.model.Folder;
import com.noteappapi.model.ResponseData;

public interface FolderService {
	ResponseData ListFolder(Integer authId, String token);

	ResponseData CreateFolder(Integer authId, String token, Folder folder);

	ResponseData GetByIdFolders(Integer id, String token);

	ResponseData UpdateFolders(Integer id, String token, Folder folder);

	ResponseData DeleteFolder(Integer id, String token);
}
