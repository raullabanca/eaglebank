package com.eaglebank;

import static com.eaglebank.testutils.TestCommons.toJson;
import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.AuthRequestDto;
import com.eaglebank.dto.AuthResponseDto;
import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.UpdateBankAccountRequestDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class BankAccountITests extends BaseIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                buildUserRequestDto())))
                .andExpect(status().isCreated());

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        this.token = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class
                )
                .token();
    }

    @Test
    void testBankAccountCRUD()
            throws Exception {
        final var createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new CreateBankAccountRequestDto("Personal bank account",
                                        AccountType.PERSONAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Personal bank account"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.sortCode").exists())
                .andExpect(jsonPath("$.createdTimestamp").exists())
                .andExpect(jsonPath("$.updatedTimestamp").exists())
                .andReturn();

        final var accountNumber = objectMapper.readValue(
                        createResponse.getResponse()
                                .getContentAsString(), BankAccountResponseDto.class
                )
                .accountNumber();

        mockMvc.perform(get("/v1/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value("Personal bank account"))
                .andExpect(jsonPath("$[*].accountType").value("personal"))
                .andExpect(jsonPath("$[*].accountNumber").isNotEmpty());

        mockMvc.perform(get("/v1/accounts/" + accountNumber)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Personal bank account"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.accountNumber").isNotEmpty());

        mockMvc.perform(patch("/v1/accounts/" + accountNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new UpdateBankAccountRequestDto("Personal bank account updated",
                                        AccountType.PERSONAL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Personal bank account updated"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.accountNumber").isNotEmpty());
    }

    @Test
    void createBankAccount_withMissingArgs_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new CreateBankAccountRequestDto(null, AccountType.PERSONAL))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBankAccounts_withNoToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBankAccount_withNoToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/v1/accounts/123456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBankAccount_nonExistingAccountNumber_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/accounts/01000000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBankAccount_invalidAccountNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/v1/accounts/00000000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBankAccount_anotherUserBankAccount_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                buildUserRequestDto("Maria", "maria@email.com", "password123"))))
                .andExpect(status().isCreated());

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                new AuthRequestDto("maria@email.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        final var token2 = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class
                )
                .token();

        final var createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new CreateBankAccountRequestDto("Personal bank account",
                                        AccountType.PERSONAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Personal bank account"))
                .andReturn();

        final var accountNumber = objectMapper.readValue(
                        createResponse.getResponse()
                                .getContentAsString(), BankAccountResponseDto.class
                )
                .accountNumber();

        mockMvc.perform(get("/v1/accounts/" + accountNumber)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBankAccount_withNoToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/v1/accounts/123456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBankAccount_nonExistingAccountNumber_shouldReturnNotFound() throws Exception {
        mockMvc.perform(patch("/v1/accounts/01000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new UpdateBankAccountRequestDto("Personal bank account updated",
                                        AccountType.PERSONAL))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBankAccount_invalidAccountNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/v1/accounts/00000000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBankAccount_anotherUserBankAccount_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                buildUserRequestDto("Gabriel", "gabriel@email.com", "password123"))))
                .andExpect(status().isCreated());

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(
                                new AuthRequestDto("gabriel@email.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        final var token2 = objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class
                )
                .token();

        final var createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(
                                new CreateBankAccountRequestDto("Personal bank account",
                                        AccountType.PERSONAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Personal bank account"))
                .andReturn();

        final var accountNumber = objectMapper.readValue(
                        createResponse.getResponse()
                                .getContentAsString(), BankAccountResponseDto.class
                )
                .accountNumber();

        mockMvc.perform(patch("/v1/accounts/" + accountNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token2)
                        .content(toJson(
                                new UpdateBankAccountRequestDto("Personal bank account updated",
                                        AccountType.PERSONAL))))
                .andExpect(status().isForbidden());
    }
}
