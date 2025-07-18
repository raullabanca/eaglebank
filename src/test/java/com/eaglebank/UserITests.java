package com.eaglebank;

import static com.eaglebank.testutils.UserTestCommons.ADDRESS_DTO;
import static com.eaglebank.testutils.UserTestCommons.COUNTY;
import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.NAME;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.PHONE_NUMBER;
import static com.eaglebank.testutils.UserTestCommons.TOWN;
import static com.eaglebank.testutils.UserTestCommons.buildUpdateUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.generateUserId;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.AddressDto;
import com.eaglebank.dto.AuthRequestDto;
import com.eaglebank.dto.AuthResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class UserITests extends BaseIntegrationTest {

    private String createdUserId;

    @Test
    void testUserCRUD() throws Exception {
        final String updatedEmail = "updated@example.com";
        final var patchRequest = new UpdateUserRequestDto(null, updatedEmail, null, null, null);

        final var createResponse = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.address.line1").value(ADDRESS_DTO.line1()))
                .andExpect(jsonPath("$.address.line2").value(ADDRESS_DTO.line2()))
                .andExpect(jsonPath("$.address.line3").value(ADDRESS_DTO.line3()))
                .andExpect(jsonPath("$.address.town").value(ADDRESS_DTO.town()))
                .andExpect(jsonPath("$.address.county").value(ADDRESS_DTO.county()))
                .andExpect(jsonPath("$.address.postcode").value(ADDRESS_DTO.postcode()))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_NUMBER))
                .andExpect(jsonPath("$.createdTimestamp").exists())
                .andExpect(jsonPath("$.updatedTimestamp").exists())
                .andReturn();

        final var createdUser = objectMapper.readValue(
                createResponse.getResponse()
                        .getContentAsString(), UserResponseDto.class);
        createdUserId = createdUser.id();

        var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(get("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

        mockMvc.perform(patch("/v1/users/" + createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(updatedEmail));

        mockMvc.perform(get("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRequestDto(updatedEmail, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(delete("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRequestDto(updatedEmail, PASSWORD))))
                .andExpect(status().isNotFound());
    }


    @Test
    void getUser_withExistingIdAndWrongToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + "wrongToken"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_withExistingIdAndNoToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/v1/users/" + createdUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_withOtherExistingIdAndValidToken_shouldReturnForbidden()
            throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andReturn();

        final var user2 = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildUserRequestDto("Maria", "maria@email.com", "Pass12"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andReturn();

        final var otherId = objectMapper.readValue(
                        user2.getResponse()
                                .getContentAsString(), UserResponseDto.class)
                .id();

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        final var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(get("/v1/users/" + otherId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_withExistingEmail_shouldReturnUserAlreadyInUse()
            throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andReturn();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withMissingData_shouldReturnBadRequest() throws Exception {
        final CreateUserRequestDto userRequestDto = new CreateUserRequestDto(null, ADDRESS_DTO,
                PHONE_NUMBER, EMAIL, PASSWORD);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldNotLogOffUserAfterUpdate_shouldReturnUpdatedUser() throws Exception {
        final UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto("Maria", null,
                null,
                null, null);

        final var token = generateToken();

        mockMvc.perform(patch("/v1/users/" + createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateUserRequestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria"));
    }

    @Test
    void updateUser_missingData_shouldReturnUpdatedUser() throws Exception {
        final UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto(null, EMAIL,
                null,
                null, PASSWORD);

        final var token = generateToken();

        mockMvc.perform(patch("/v1/users/" + createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateUserRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_addressWithMissingData_shouldReturnBadRequest() throws Exception {
        final AddressDto requestAddressDto = new AddressDto(null, null, null, TOWN, COUNTY, null);
        final UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto(null, EMAIL,
                requestAddressDto,
                null, PASSWORD);

        final var token = generateToken();

        mockMvc.perform(patch("/v1/users/" + createdUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateUserRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_anotherId_shouldReturnForbidden() throws Exception {
        final UpdateUserRequestDto updateUserRequestDto = buildUpdateUserRequestDto();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL));

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        final var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(patch("/v1/users/" + generateUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateUserRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_missingData_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL));

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        final var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(delete("/v1/users/null")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_anotherId_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL));

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        final var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();

        mockMvc.perform(delete("/v1/users/" + generateUserId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withOpenBankAccount_shouldReturnConflict() throws Exception {
        final var token = generateToken();

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(
                                new CreateBankAccountRequestDto("Personal bank account",
                                        AccountType.PERSONAL))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private String generateToken() throws Exception {
        var createResponse = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andReturn();

        var createdUser = objectMapper.readValue(
                createResponse.getResponse()
                        .getContentAsString(), UserResponseDto.class);
        createdUserId = createdUser.id();

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        final var token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class)
                .token();
        return token;
    }
}
