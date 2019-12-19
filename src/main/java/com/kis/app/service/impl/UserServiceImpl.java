package com.kis.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kis.app.exceptions.UserServiceException;
import com.kis.app.io.entity.PasswordResetTokenEntity;
import com.kis.app.io.entity.UserEntity;
import com.kis.app.io.repositories.PasswordResetTokenRepository;
import com.kis.app.io.repositories.UserRepository;
import com.kis.app.service.UserService;
import com.kis.app.shared.AmazonSES;
import com.kis.app.shared.Utils;
import com.kis.app.shared.dto.AddressDTO;
import com.kis.app.shared.dto.UserDto;
import com.kis.app.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired // create the instance object
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	AmazonSES amazonSES;
	
	

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new UserServiceException("Record already exists");

		for (int i = 0; i < user.getAddresses().size(); i++) {
			AddressDTO address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(i, address);
		}

		// BeanUtils.copyProperties(user, userEntity);
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);

		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);

		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(storedUserDetails, UserDto.class);

		// send an email to client to very there own email address
		amazonSES.verifyEmail(returnValue);

		return returnValue;
	}

	@Override
	public UserDto getUser(String email) {

		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;

	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
				userEntity.getEmailVerificationStatus(), true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUserById(String userId) {

		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("user with id: " + userId + " not found");
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {

		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		UserEntity updatedUser = userRepository.save(userEntity);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);

	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> returnValue = new ArrayList<>();
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();

		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}

		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		boolean returnValue = false;
		// find the user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		if (userEntity != null) {
			boolean hastokenExpired = Utils.hasTokenExpired(token);
			if (!hastokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}

		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean returnVal = false;

		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			return returnVal;

		String token = new Utils().generatePasswordResetToken(userEntity.getUserId());

		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);

		returnVal = new AmazonSES().sendPasswordResetRequest(userEntity.getFirstName(), userEntity.getEmail(), token);

		return returnVal;
	}

	@Override
	public boolean resetPassword(String token, String password) {

		boolean returnVal = false;

		if (Utils.hasTokenExpired(token)) {
			return returnVal;
		}

		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

		if (passwordResetTokenEntity == null) {
			return returnVal;
		}

		// prepare new password
		String encodedPassword = bCryptPasswordEncoder.encode(password);

		// Update user password on database
		UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		UserEntity savedUserEntity = userRepository.save(userEntity);

		// verify if password was saved
		if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
			returnVal = true;
		}
		
		passwordResetTokenRepository.delete(passwordResetTokenEntity);

		return returnVal;
	}

}
