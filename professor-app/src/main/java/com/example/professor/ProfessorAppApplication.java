package com.example.professor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.professor.entity")
@EnableJpaRepositories("com.example.professor.repository")
public class ProfessorAppApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProfessorAppApplication.class, args);

	}

}
