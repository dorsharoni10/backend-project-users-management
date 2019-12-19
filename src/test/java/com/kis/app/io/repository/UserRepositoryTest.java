//package com.kis.app.io.repository;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import com.kis.app.io.entity.AddressEntity;
//import com.kis.app.io.entity.UserEntity;
//import com.kis.app.io.repositories.UserRepository;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//class UserRepositoryTest {
//
//	@Autowired
//	UserRepository userRepository;
//
//	private static boolean isRecordCreated = false;
//
//	@BeforeEach
//	void setUp() throws Exception {
//		if (!isRecordCreated) {
//			CreateRecord();
//		}
//
//	}
//
//	@Test
//	final void testGetVerifiedUsers() {
//		Pageable pageableRequest = PageRequest.of(0, 2);
//		Page<UserEntity> pages = userRepository.findAllUsersWithConfirmedEmailAddress(pageableRequest);
//		assertNotNull(pages);
//
//		List<UserEntity> userEntities = pages.getContent();
//		assertNotNull(userEntities);
//		assertTrue(userEntities.size() > 1);
//
//	}
//
//	@Test
//	final void testFindUserByFirstName() {
//
//		String firstName = "Dor";
//		List<UserEntity> users = userRepository.findUserByFirstName(firstName);
//
//		assertNotNull(users);
//
//		assertTrue(users.size() > 1);
//		UserEntity user = users.get(0);
//		assertTrue(user.getFirstName().equals(firstName));
//
//	}
//
//	@Test
//	final void testFindUserByLastName() {
//
//		String lastName = "Sharoni";
//		List<UserEntity> users = userRepository.findUserByLastName(lastName);
//
//		assertNotNull(users);
//
//		assertTrue(users.size() > 1);
//		UserEntity user = users.get(0);
//		assertTrue(user.getLastName().equals(lastName));
//
//	}
//	
//	@Test
//	final void testFindUserThatContainKeyword() {
//
//		String keyword = "ron";
//		List<UserEntity> users = userRepository.findUserThatContainKeyword(keyword);
//
//		assertNotNull(users);
//
//		assertTrue(users.size() > 1);
//		UserEntity user = users.get(0);
//		assertTrue(user.getLastName().contains(keyword) || user.getFirstName().contains(keyword));
//
//	}
//	
//	@Test
//	final void testFindUserThatContainKeywordFirstAndLastName() {
//
//		String keyword = "ron";
//		List<Object[]> users = userRepository.findUserThatContainKeywordFirstAndLastName(keyword);
//
//		assertNotNull(users);
//
//		assertTrue(users.size() > 1);
//		Object[] user = users.get(0);
//		String userFirstName = String.valueOf(user[0]);
//		String userLastName = String.valueOf(user[1]);
//		
//		assertNotNull(userFirstName);
//		assertNotNull(userLastName);
//
//		System.out.println("First Name = " + userFirstName);
//		System.out.println("Last Name = " + userLastName);
//
//
//	}
//	
//	@Test
//	final void testUpdateUserEmailVerificationStatus()
//	{
//		boolean updateEmailVerificationStatus = false;
//		
//		userRepository.updateUserEmailVerificationStatus(updateEmailVerificationStatus, "fsaf324");
//		
//		UserEntity userEntity = userRepository.findByUserId("fsaf324");
//		boolean storedEmailVerificationStatus = userEntity.getEmailVerificationStatus();
//		
//		assertTrue(storedEmailVerificationStatus == updateEmailVerificationStatus); 
//	}
//	
//	
//	@Test
//	final void testFindUserEntityByUserId()
//	{
//		String userId = "fsaf324";
//		UserEntity userEntity = userRepository.findUserEntityByUserId(userId);
//		
//		assertNotNull(userEntity);
//		assertTrue(userEntity.getUserId().equals(userId));
//	}
//	
//	@Test
//	final void testGetUserEntityFullNameById()
//	{
//		String userId = "fsaf324";
//		List<Object[]> records = userRepository.getUserEntityFullNameById(userId);
//		
//		assertNotNull(records);
//		assertTrue(records.size()==1);
//		
//		Object[] userDetails =records.get(0);
//		
//		String firstName = String.valueOf(userDetails[0]);
//		String lastName = String.valueOf(userDetails[1]);
//		
//		assertNotNull(firstName);
//		assertNotNull(lastName);
//	}
//	
////	
////	@Test
////	final void testUpdateUserEntityEmailVerificationStatus()
////	{
////		boolean updateEmailVerificationStatus = false;
////		
////		userRepository.updateUserEntityEmailVerificationStatus(updateEmailVerificationStatus, "fsaf324");
////		
////		UserEntity userEntity = userRepository.findByUserId("fsaf324");
////		boolean storedEmailVerificationStatus = userEntity.getEmailVerificationStatus();
////		
////		assertTrue(storedEmailVerificationStatus == updateEmailVerificationStatus); 
////	}
//	
//	
//
//	private void CreateRecord() {
//
//		// put userEntity on sql table
//		UserEntity userEntity = new UserEntity();
//		userEntity.setFirstName("Dor");
//		userEntity.setLastName("Sharoni");
//		userEntity.setUserId("fsaf324");
//		userEntity.setEncryptedPassword("fdss43sad");
//		userEntity.setEmail("dor989@gmail.com");
//		userEntity.setEmailVerificationStatus(true);
//
//		AddressEntity addressEntity = new AddressEntity();
//		addressEntity.setType("shipping");
//		addressEntity.setAddressId("sad23");
//		addressEntity.setCity("Netanya");
//		addressEntity.setCountry("Israel");
//		addressEntity.setPostalCode("213214");
//		addressEntity.setStreetName("Steet Name 44");
//		List<AddressEntity> addresses = new ArrayList<>();
//		addresses.add(addressEntity);
//
//		userEntity.setAddresses(addresses);
//
//		userRepository.save(userEntity);
//
//		// put one more userEntity on sql table
//		UserEntity userEntity2 = new UserEntity();
//		userEntity2.setFirstName("Dor");
//		userEntity2.setLastName("Sharoni");
//		userEntity2.setUserId("dsag32");
//		userEntity2.setEncryptedPassword("fdss43sad");
//		userEntity2.setEmail("dor989@gmail.com");
//		userEntity2.setEmailVerificationStatus(true);
//
//		AddressEntity addressEntity2 = new AddressEntity();
//		addressEntity2.setType("shipping");
//		addressEntity2.setAddressId("sad23");
//		addressEntity2.setCity("Netanya");
//		addressEntity2.setCountry("Israel");
//		addressEntity2.setPostalCode("213214");
//		addressEntity2.setStreetName("Steet Name 44");
//		List<AddressEntity> addresses2 = new ArrayList<>();
//		addresses2.add(addressEntity2);
//
//		userEntity2.setAddresses(addresses2);
//
//		userRepository.save(userEntity2);
//
//		isRecordCreated = true;
//	}
//
//}
