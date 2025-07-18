package com.eaglebank;

import static com.eaglebank.testutils.TestCommons.toJson;
import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.NAME;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.AuthRequestDto;
import com.eaglebank.dto.AuthResponseDto;
import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.dto.TransactionType;
import com.eaglebank.dto.UserResponseDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class TransactionITests extends BaseIntegrationTest {

    public static final String CURRENCY = "GBP";

    private UserResponseDto user;
    private String token;

    private BankAccountResponseDto bankAccount;
    private TransactionResponseDto transaction;

    @BeforeEach
    void setup() throws Exception {
        this.token = createNewAccountAndGetToken(NAME, EMAIL);
        this.bankAccount = createBankAccount("Personal bank account", token);
        createTransaction();
    }

    @Test
    void createTransaction_validDeposit_shouldReturnTransaction() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("100.00",
                TransactionType.DEPOSIT.name(), CURRENCY);

        mockMvc.perform(post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.currency").value(CURRENCY));
    }

    @Test
    void createTransaction_validWithdraw_shouldReturnTransaction() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("50.00",
                TransactionType.WITHDRAWAL.name(), CURRENCY);

        mockMvc.perform(post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.type").value("withdrawal"))
                .andExpect(jsonPath("$.currency").value(CURRENCY));
    }

    @Test
    void createTransaction_invalidCurrency_shouldReturnTransaction() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("50.00",
                TransactionType.WITHDRAWAL.name(), "USD");

        mockMvc.perform(post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_invalidWithdraw_shouldReturnUnprocessableEntity() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("200.00",
                TransactionType.WITHDRAWAL.name(), CURRENCY);

        mockMvc.perform(post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createTransaction_anotherBankAccount_shouldReturnForbidden() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("100.00",
                TransactionType.DEPOSIT.name(), CURRENCY);
        final String otherUser = "usr-other";
        final String otherEmail = "other@email.com";
        final var otherToken = createNewAccountAndGetToken(otherUser, otherEmail);

        mockMvc.perform(post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransactions_allCorrect_shouldReturnTransactionList() throws Exception {
        mockMvc.perform(get("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]").isArray())
                .andExpect(jsonPath("$[*].id").value(transaction.id()))
                .andExpect(jsonPath("$[*].amount").exists());
    }

    @Test
    void getTransactions_anotherBankAccount_shouldReturnForbidden() throws Exception {
        final String otherUser = "usr-other";
        final String otherEmail = "other@email.com";
        final var otherToken = createNewAccountAndGetToken(otherUser, otherEmail);

        mockMvc.perform(get("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransaction_allCorrect_shouldReturnTransaction() throws Exception {
        mockMvc.perform(get("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions/" +
                            transaction.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.id()))
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void getTransaction_anotherBankAccount_shouldReturnForbidden() throws Exception {
        final String otherUser = "usr-other";
        final String otherEmail = "other@email.com";
        final var otherToken = createNewAccountAndGetToken(otherUser, otherEmail);

        final var bankAccountResponse = createBankAccount(otherUser, otherToken);

        mockMvc.perform(
                        get("/v1/accounts/" + bankAccountResponse.accountNumber() + "/transactions/" +
                            transaction.id())
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransaction_transactionDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/accounts/01234567/transactions/tan-doesnotexist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransaction_transactionNotBelongToAccount_shouldReturnNotFound() throws Exception {
        final String otherUser = "usr-other";
        final String otherEmail = "other@email.com";
        final var otherToken = createNewAccountAndGetToken(otherUser, otherEmail);

        final var bankAccountResponse = createBankAccount(otherUser, otherToken);

        final CreateTransactionRequestDto request = buildTransactionRequest("100.00",
                TransactionType.DEPOSIT.name(), CURRENCY);

        mockMvc.perform(
                        post("/v1/accounts/" + bankAccountResponse.accountNumber() + "/transactions")
                                .header("Authorization", "Bearer " + otherToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(
                        get("/v1/accounts/" + bankAccountResponse.accountNumber() + "/transactions/" +
                            transaction.id())
                                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    private String createNewAccountAndGetToken(final String user, final String email)
            throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(buildUserRequestDto(user, email, PASSWORD))))
                .andExpect(status().isCreated())
                .andReturn();

        final var authTokenResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AuthRequestDto(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                        authTokenResponse.getResponse()
                                .getContentAsString(), AuthResponseDto.class
                )
                .token();
    }

    private BankAccountResponseDto createBankAccount(final String name, final String userToken)
            throws Exception {
        final var response = mockMvc.perform(post("/v1/accounts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CreateBankAccountRequestDto(name, AccountType.PERSONAL))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(response.getResponse()
                .getContentAsString(), BankAccountResponseDto.class);
    }

    private void createTransaction() throws Exception {
        final CreateTransactionRequestDto request = buildTransactionRequest("100.00",
                TransactionType.DEPOSIT.name(), CURRENCY);

        final var transactionResponse = mockMvc.perform(
                        post("/v1/accounts/" + this.bankAccount.accountNumber() + "/transactions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        transaction = objectMapper.readValue(
                transactionResponse.getResponse()
                        .getContentAsString(), TransactionResponseDto.class
        );
    }

    private CreateTransactionRequestDto buildTransactionRequest(final String amount,
            final String type,
            final String currency) {
        return new CreateTransactionRequestDto(new BigDecimal(amount), currency,
                TransactionType.valueOf(type.toUpperCase()), "Test " + type);
    }
}
