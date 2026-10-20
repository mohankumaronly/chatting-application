package com.rockranger.chat;

import com.rockranger.chat.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatApplication {

	public static void main(String[] args) {
		DotenvLoader.loadToMap();
		SpringApplication.run(ChatApplication.class, args);
	}

}