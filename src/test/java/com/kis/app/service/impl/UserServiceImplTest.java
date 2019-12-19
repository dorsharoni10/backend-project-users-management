package com.kis.app.service.impl;

import static org.mockito.Mockito.times;	
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kis.app.exceptions.UserServiceException;
import com.kis.app.io.entity.AddressEntity;
import com.kis.app.io.entity.UserEntity;
import com.kis.app.io.repositories.UserRepository;
import com.kis.app.service.impl.UserServiceImpl;
import com.kis.app.shared.AmazonSES;
import com.kis.app.shared.Utils;
import com.kis.app.shared.dto.AddressDTO;
import com.kis.app.shared.dto.UserDto;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userServiceImpl;

	@Mock
	UserRepository userRepository;

	@Mock
	Utils utils;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Mock
	AmazonSES amazonSES;

	UserEntity entity;
	String userId = "safads4324sad";
	String encryptedPassword = "fdsf4322fds";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		entity = new UserEntity();
		entity.setId(1L);
		entity.setFirstName("Dor");
		entity.setLastName("Sharoni");
		entity.setUserId(userId);
		entity.setEncryptedPassword(encryptedPassword);
		entity.setEmail("test@gmail.com");
		entity.setEmailVerificationToken("dsaf23tfsd");
		entity.setAddresses(getAddressesEntity());
	}

	@Test
	final void testGetUser() {

		when(userRepository.findByEmail(anyString())).thenReturn(entity);

		UserDto userDto = userServiceImpl.getUser("dsad@gmail.com");
		assertNotNull(userDto);
		assertEquals("Dor", userDto.getFirstName());

	}

	@Test
	final void testCreateUser_CreateUserServiceException() {

		when(userRepository.findByEmail(anyString())).thenReturn(entity);
		
		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDTO());
		userDto.setFirstName("Dor");
		userDto.setLastName("Sharoni");
		userDto.setPassword("1234");
		userDto.setEmail("test@gmail.com");

		assertThrows(UserServiceException.class,

				() -> {
					userServiceImpl.createUser(userDto);
				});
	}

	@Test
	final void testGetUser_UsernameNotFoundException() {

		when(userRepository.findByEmail(anyString())).thenReturn(null);

		assertThrows(UsernameNotFoundException.class,

				() -> {
					userServiceImpl.getUser("dsad@gmail.com");
				});
	}

	@Test
	final void testCreateUser() {
		when(userRepository.findByEmail(anyString())).thenReturn(null);
		when(utils.generateAddressId(anyInt())).thenReturn("dsafaff234d");
		when(utils.generateUserId(anyInt())).thenReturn(userId);
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
		when(userRepository.save(any(UserEntity.class))).thenReturn(entity);
		Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDto.class));

		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDTO());
		userDto.setFirstName("Dor");
		userDto.setLastName("Sharoni");
		userDto.setPassword("1234");
		userDto.setEmail("test@gmail.com");

		UserDto storedUserDetails = userServiceImpl.createUser(userDto);
		assertNotNull(storedUserDetails);
		assertEquals(entity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(entity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), entity.getAddresses().size());
		verify(utils, times(storedUserDetails.getAddresses().size())).generateAddressId(30);
		verify(bCryptPasswordEncoder, times(1)).encode("1234");
		verify(userRepository, times(1)).save(any(UserEntity.class));
	}

	private List<AddressDTO> getAddressesDTO() {

		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setType("shipping");
		addressDTO.setCity("Netanya");
		addressDTO.setCountry("Israel");
		addressDTO.setPostalCode("dsa3rq");
		addressDTO.setStreetName("shivti israel 1 a");

		AddressDTO billingAddressDTO = new AddressDTO();
		addressDTO.setType("billing");
		addressDTO.setCity("Netanya");
		addressDTO.setCountry("Israel");
		addressDTO.setPostalCode("dsa3rq");
		addressDTO.setStreetName("shivti israel 1 a");

		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(addressDTO);
		addresses.add(billingAddressDTO);

		return addresses;
	}

	private List<AddressEntity> getAddressesEntity() {
		List<AddressDTO> addresses = getAddressesDTO();

		Type listType = new TypeToken<List<AddressEntity>>() {
		}.getType();

		return new ModelMapper().map(addresses, listType);
	}
}
