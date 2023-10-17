package com.noteappapi.config;

import com.noteappapi.model.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {
	private final Email email;

	public EmailConfig(Email email) {
		this.email = email;
	}

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(email.getHost());
		mailSender.setPort(email.getPort());
		mailSender.setUsername(email.getUsername()); // Sử dụng giá trị từ EmailConfig
		mailSender.setPassword(email.getPassword()); // Sử dụng giá trị từ EmailConfig

		// Cấu hình properties
		Properties properties = mailSender.getJavaMailProperties();
		properties.put("mail.smtp.auth", email.isSmtpAuth());
		properties.put("mail.smtp.starttls.enable", email.isStarttlsEnable());

		return mailSender;
	}
}
