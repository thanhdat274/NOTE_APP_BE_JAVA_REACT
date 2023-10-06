package com.noteappapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.noteappapi.util.ObjectUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notes") // Điều này phụ thuộc vào tên bảng trong cơ sở dữ liệu của bạn
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;

	@ManyToOne
	@JoinColumn(name = "folder_id", referencedColumnName = "id")
	private Folder folderId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

	@Override
	public String toString() {
		return ObjectUtil.convertToString(this);
	}
}
