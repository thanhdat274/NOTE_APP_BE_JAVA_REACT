package com.noteappapi;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class NoteAppApiApplication {

	public static void main(String[] args) {
		log.info("Starting note app with configuration");
		MDC.put("tracking", NanoIdUtils.randomNanoId());
		SpringApplication.run(NoteAppApiApplication.class, args);
	}

}
