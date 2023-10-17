package com.noteappapi.model;

import com.noteappapi.util.ObjectUtil;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class Email {
	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private int port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private boolean smtpAuth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private boolean starttlsEnable;

	@Override
	public String toString() {
		return ObjectUtil.convertToString(this);
	}
}
