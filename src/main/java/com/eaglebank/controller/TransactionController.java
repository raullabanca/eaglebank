package com.eaglebank.controller;


import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{accountNumber}/transactions")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Pattern(regexp = "^01\\d{6}$")
            @PathVariable String accountNumber,

            @Valid @RequestBody CreateTransactionRequestDto requestDto,

            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(
                        accountNumber, user.getId(), requestDto
                ));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(
            @PathVariable
            @Pattern(regexp = "^01\\d{6}$", message = "Invalid account number format")
            String accountNumber,
            @AuthenticationPrincipal AuthenticatedUser authUser) {

        List<TransactionResponseDto> response =
                transactionService.getTransactions(accountNumber, authUser.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransaction(
            @PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
            @PathVariable @Pattern(regexp = "^tan-[A-Za-z0-9]+$") String transactionId,
            @AuthenticationPrincipal AuthenticatedUser authUser) {

        TransactionResponseDto response = transactionService.getTransaction(
                authUser.getId(), accountNumber, transactionId
        );

        return ResponseEntity.ok(response);
    }
}

