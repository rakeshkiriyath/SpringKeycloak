package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class StudentController {

	private final UserRepository userRepository;

	public StudentController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/")
	public ModelAndView index() {
		return new ModelAndView("redirect:/home");
	}

	@GetMapping("/home")
	public ModelAndView home(@AuthenticationPrincipal OidcUser principal) {
		ModelAndView modelAndView = new ModelAndView("home");
		modelAndView.addObject("firstName", principal.getGivenName());
		modelAndView.addObject("lastName", principal.getFamilyName());
		modelAndView.addObject("email", principal.getEmail());
		modelAndView.addObject("username", principal.getPreferredUsername());
		return modelAndView;
	}

	@GetMapping("/manageStudents")
	public ModelAndView manageStudents() {
		ModelAndView modelAndView = new ModelAndView("manageStudents");
		List<User> students = userRepository.findByRole("STUDENT");
		modelAndView.addObject("students", students);
		return modelAndView;
	}

	@GetMapping("/access-denied")
	public ModelAndView accessDenied() {
		ModelAndView modelAndView = new ModelAndView("access-denied");
		modelAndView.setStatus(HttpStatus.FORBIDDEN);
		return modelAndView;
	}
}
