package com.eaglebank.dto;

import java.time.OffsetDateTime;

public record UserResponseDto(
        String id,
        String name,
        AddressDto address,
        String phoneNumber,
        String email,
        OffsetDateTime createdTimestamp,
        OffsetDateTime updatedTimestamp
) {

}

