package com.denidove.Logistics;

import com.denidove.Logistics.entities.Role;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogisticsApplication {

	private final UserService userService;

	public LogisticsApplication(UserService userService) {
		this.userService = userService;
	}

	private static final Logger log = LoggerFactory.getLogger(LogisticsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LogisticsApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			log.info("Command line started");
			log.info("");

			/*
			User user = new User();
			user.setName("Даша");
			user.setLogin("dasha");
			user.setPassword("Kisochka7");
			user.setAge(24);
			userService.save(user);
			*/
		};
	}
}
