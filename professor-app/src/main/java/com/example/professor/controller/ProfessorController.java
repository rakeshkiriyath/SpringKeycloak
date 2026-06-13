package com.example.professor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.professor.entity.User;
import com.example.professor.repository.UserRepository;

@Controller
public class ProfessorController {

	private final UserRepository userRepository;

	public ProfessorController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	

	@GetMapping("/manageStudents")
	public ModelAndView manageStudents() {
		ModelAndView modelAndView = new ModelAndView("manageStudents");
		List<User> students = userRepository.findByRole("STUDENT");
		modelAndView.addObject("students", students);
		return modelAndView;
	}

	@GetMapping("/updateStudent/{id}")
	public ModelAndView showUpdateForm(@PathVariable Long id) {
		ModelAndView modelAndView = new ModelAndView("updateStudent");
		User student = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
		modelAndView.addObject("student", student);
		return modelAndView;
	}

	@PostMapping("/updateStudent/{id}")
	public String updateStudent(@PathVariable Long id, @ModelAttribute User formUser) {
		User student = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

		student.setFirstName(formUser.getFirstName());
		student.setLastName(formUser.getLastName());
		student.setEmail(formUser.getEmail());
		student.setUsername(formUser.getUsername());

		userRepository.save(student);
		return "redirect:/manageStudents";
	}

	@PostMapping("/deleteStudent/{id}")
	public String deleteStudent(@PathVariable Long id) {
		userRepository.deleteById(id);
		return "redirect:/manageStudents";
	}

	@GetMapping("/access-denied")
	public ModelAndView accessDenied() {
		ModelAndView modelAndView = new ModelAndView("access-denied");
		modelAndView.setStatus(HttpStatus.FORBIDDEN);
		return modelAndView;
	}
}
