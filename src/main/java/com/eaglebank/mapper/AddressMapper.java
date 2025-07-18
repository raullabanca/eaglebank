package com.eaglebank.mapper;

import com.eaglebank.dto.AddressDto;
import com.eaglebank.model.Address;

public class AddressMapper {

    public static Address toEntity(final AddressDto dto) {
        return new Address(dto.line1(), dto.line2(), dto.line3(), dto.town(), dto.county(),
                dto.postcode());
    }

    public static AddressDto toDto(final Address address) {
        return new AddressDto(address.getLine1(), address.getLine2(), address.getLine3(),
                address.getTown(), address.getCounty(), address.getPostcode());
    }
}
