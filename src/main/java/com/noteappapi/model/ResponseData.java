package com.noteappapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.noteappapi.util.ObjectUtil;
import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData {
	private String code;
	private String message;
	private Map<String, Object> data;

	@Override
	public String toString() {
		return ObjectUtil.convertToString(this);
	}
}
