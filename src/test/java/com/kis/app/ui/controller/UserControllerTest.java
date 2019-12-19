package com.kis.app.ui.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.kis.app.service.impl.UserServiceImpl;
import com.kis.app.shared.dto.AddressDTO;
import com.kis.app.shared.dto.UserDto;
import com.kis.app.ui.controller.UserController;
import com.kis.app.ui.model.response.UserRest;

import static org.mockito.ArgumentMatchers.*;

class UserControllerTest {

	@InjectMocks
	UserController userController;

	@Mock
	UserServiceImpl userService;

	UserDto userDto;

	final String USER_ID = "bnmsb234sa";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		userDto = new UserDto();
		userDto.setAddresses(getAddressesDTO());
		userDto.setFirstName("Dor");
		userDto.setLastName("Sharoni");
		// userDto.setPassword("1234");
		userDto.setEmail("test@gmail.com");
		userDto.setEmailVerificationToken(null);
		userDto.setEmailVerificationStatus(Boolean.FALSE);
		userDto.setUserId(USER_ID);
		userDto.setEncryptedPassword("dsacxzc324");

	}

	@Test
	final void testGetUser() {
		when(userService.getUserById(anyString())).thenReturn(userDto);
		UserRest userRest = userController.getUser(USER_ID);
		assertNotNull(userRest);
		assertEquals(USER_ID, userRest.getUserId());
		assertEquals(userDto.getFirstName(), userRest.getFirstName());
		assertEquals(userDto.getLastName(), userRest.getLastName());
		assertTrue(userDto.getAddresses().size() == userRest.getAddresses().size());

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

}
