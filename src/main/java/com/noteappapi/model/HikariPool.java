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
public class HikariPool {
	@Value("${spring.datasource.hikari.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.hikari.jdbc-url}")
	private String jdbcUrl;

	@Value("${spring.datasource.hikari.username}")
	private String username;

	@Value("${spring.datasource.hikari.password}")
	private String password;

	@Value("${spring.datasource.hikari.connectionTimeout}")
	private int connectionTimeout;

	@Value("${spring.datasource.hikari.idleTimeout}")
	private int idleTimeout;

	@Value("${spring.datasource.hikari.maxLifetime}")
	private int maxLifetime;

	@Value("${spring.datasource.hikari.minimumIdle}")
	private int minimumIdle;

	@Value("${spring.datasource.hikari.maximumPoolSize}")
	private int maximumPoolSize;

	@Override
	public String toString() {
		return ObjectUtil.convertToString(this);
	}
}