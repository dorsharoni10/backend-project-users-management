package com.kis.app.ui.controller;

import java.lang.reflect.Type;				
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kis.app.exceptions.UserServiceException;
import com.kis.app.service.AddressService;
import com.kis.app.service.UserService;
import com.kis.app.shared.dto.AddressDTO;
import com.kis.app.shared.dto.UserDto;
import com.kis.app.ui.model.request.PasswordResetModel;
import com.kis.app.ui.model.request.PasswordResetRequestModel;
import com.kis.app.ui.model.request.UserDetailsRequestModel;
import com.kis.app.ui.model.response.AddressesRest;
import com.kis.app.ui.model.response.ErrorMessages;
import com.kis.app.ui.model.response.OperationStatusModel;
import com.kis.app.ui.model.response.RequestOperationName;
import com.kis.app.ui.model.response.RequestOperationStatus;
import com.kis.app.ui.model.response.UserRest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/users") // http://localhost:8080/users
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressesService;
	
	@Autowired
	AddressService addressService; 
	


	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();
		UserDto userDto = userService.getUserById(id);
		returnValue = new ModelMapper().map(userDto, UserRest.class);
		return returnValue;
	}	

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })

	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

		 if(userDetails.getFirstName().isEmpty())
			 throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserRest returnValue = new UserRest();
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createdUser = userService.createUser(userDto);
		returnValue = modelMapper.map(createdUser, UserRest.class);

		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {

		UserRest returnValue = new UserRest();
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updateUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updateUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {

		OperationStatusModel returnValue = new OperationStatusModel();

		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(id);

		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return returnValue;
	}

	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {

		if (page > 0)
			page -= 1;

		List<UserRest> returnValuse = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValuse.add(userModel);
		}

		return returnValuse;
	}

	// http://localhost:8080/users-management/users/userId/addresses
	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> addressesListRestModel = new ArrayList<>();
		
		List<AddressDTO> addressesDto = addressesService.getAddresses(id);
		
		if (addressesDto != null && !addressesDto.isEmpty())
		{
			Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
			addressesListRestModel = new ModelMapper().map(addressesDto, listType);
			
			for (AddressesRest addressesRest : addressesListRestModel) {
				Link addressLink = linkTo(methodOn(UserController.class)
						.getUserAddress(id, addressesRest.getAddressId())).withSelfRel();
				addressesRest.add(addressLink);
				
				Link userLink = linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
				addressesRest.add(userLink);
			}
		}

		return new CollectionModel<>(addressesListRestModel);
	}
	
	// http://localhost:8080/users-management/"userId"/addresses/"addressId"
	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public AddressesRest getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		
		AddressDTO addressDTO = addressService.getAddress(addressId);
		Link addressLink = linkTo(methodOn(UserController.class)
				.getUserAddress(userId, addressId)).withSelfRel();
		Link userLink = linkTo(UserController.class).slash(userId).withRel("user");
		Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(userId)).withRel("addresses");


		AddressesRest addressesRestModel = new ModelMapper().map(addressDTO, AddressesRest.class);
		addressesRestModel.add(addressLink);
		addressesRestModel.add(userLink);
		addressesRestModel.add(addressesLink);

		return addressesRestModel;
	
	}
	// http://localhost:8080/users-management/users/email-verification?token=sadasf
	@GetMapping(path = "/email-verification", produces = { //MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token)
	{
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		
		boolean isVerified = userService.verifyEmailToken(token);
		
		if(isVerified)
		{
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		else
		{
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}
			
		
		return returnValue;
	}
	
	// http://localhost:8080/users-management/users/password-reset-request
	@PostMapping(path = "/password-reset-request",
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestMode)
	{
		OperationStatusModel returnVal = new OperationStatusModel();
		boolean operationResult = userService.requestPasswordReset(passwordResetRequestMode.getEmail());
		returnVal.setOperationName(RequestOperationName.REQUSET_PASSWORD_RESET.name());
		returnVal.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if(operationResult)
		{
			returnVal.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return returnVal;
	}
	
	// http://localhost:8080/users-management/users/password-reset
	@PostMapping(path = "/password-reset",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel)
	{
		OperationStatusModel returnVal = new OperationStatusModel();
		boolean operationResult = userService.resetPassword(passwordResetModel.getToken(),passwordResetModel.getPassword());
		
		returnVal.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		returnVal.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if(operationResult)
		{
			returnVal.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return returnVal;
	}
	
	

}
