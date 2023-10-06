package com.noteappapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.noteappapi.util.ObjectUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "folders") // Điều này phụ thuộc vào tên bảng trong cơ sở dữ liệu của bạn
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Folder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "auth_id", referencedColumnName = "id")
	private Users authId;

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
