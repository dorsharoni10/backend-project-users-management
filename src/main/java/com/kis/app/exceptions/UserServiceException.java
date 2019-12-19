package com.kis.app.exceptions;

public class UserServiceException extends RuntimeException {

	private static final long serialVersionUID = 7159797720879793339L;

	public UserServiceException(String message)
	{
		super(message);
	}
}
