package com.eaglebank.mapper;

import static com.eaglebank.testutils.UserTestCommons.ADDRESS;
import static com.eaglebank.testutils.UserTestCommons.ADDRESS_DTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eaglebank.dto.AddressDto;
import com.eaglebank.model.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressMapperUTest {

    @Test
    void toEntity_shouldMapCorrectly() {
        final Address expected = ADDRESS;
        final Address actual = AddressMapper.toEntity(
                ADDRESS_DTO);

        assertEquals(expected.getLine1(), actual.getLine1());
        assertEquals(expected.getLine2(), actual.getLine2());
        assertEquals(expected.getLine3(), actual.getLine3());
        assertEquals(expected.getTown(), actual.getTown());
        assertEquals(expected.getCounty(), actual.getCounty());
        assertEquals(expected.getPostcode(), actual.getPostcode());
    }

    @Test
    void toDto_shouldMapCorrectly() {
        final AddressDto expected = ADDRESS_DTO;
        final AddressDto actual = AddressMapper.toDto(
                ADDRESS);

        assertEquals(expected.line1(), actual.line1());
        assertEquals(expected.line2(), actual.line2());
        assertEquals(expected.line3(), actual.line3());
        assertEquals(expected.town(), actual.town());
        assertEquals(expected.county(), actual.county());
        assertEquals(expected.postcode(), actual.postcode());
    }
}