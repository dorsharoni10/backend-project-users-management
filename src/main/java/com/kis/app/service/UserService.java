package com.kis.app.service;

import java.util.List;	

import org.springframework.security.core.userdetails.UserDetailsService;

import com.kis.app.shared.dto.UserDto;

public interface UserService extends UserDetailsService {

	UserDto createUser(UserDto user);
	UserDto getUser(String email);
	UserDto getUserById(String userId);
	UserDto updateUser(String userId, UserDto user);
	void deleteUser(String userId);
	List<UserDto> getUsers(int page, int limit);
	boolean verifyEmailToken(String token);
	boolean requestPasswordReset(String email);
	boolean resetPassword(String token, String password);
}
