package com.kis.app.io.repositories;

import org.springframework.data.repository.CrudRepository;

import com.kis.app.io.entity.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetTokenEntity, Long> {

	PasswordResetTokenEntity findByToken(String token);
	
}
