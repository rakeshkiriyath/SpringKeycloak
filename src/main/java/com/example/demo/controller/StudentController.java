package com.example.demo.controller;

import org.springframework.security.core.Authentication;
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

	@GetMapping(value = {"/","/login"})
	public ModelAndView login() {
		ModelAndView modelAndView = new ModelAndView("login");
		return modelAndView;
	}

	@GetMapping("/home")
	public ModelAndView home(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView("home");
		User user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("Logged in user not found: " + authentication.getName()));
		modelAndView.addObject("user", user);
		return modelAndView;
	}
}
