package com.kis.app.service;

import java.util.List;

import com.kis.app.shared.dto.AddressDTO;

public interface AddressService {
	List<AddressDTO> getAddresses(String userId);

	AddressDTO getAddress(String addressId);

}
