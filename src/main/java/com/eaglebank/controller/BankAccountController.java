package com.eaglebank.controller;

import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.UpdateBankAccountRequestDto;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.BankAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponseDto> createBankAccount(@Valid @RequestBody final CreateBankAccountRequestDto bankAccountRequestDto,
            @AuthenticationPrincipal final AuthenticatedUser authUser) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankAccountService.createBankAccount(bankAccountRequestDto,
                        authUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponseDto>> getBankAccounts(@AuthenticationPrincipal final AuthenticatedUser authUser) {
        return ResponseEntity.ok(bankAccountService.getBankAccounts(authUser.getId()));
    }

    @GetMapping(path = "/{accountNumber}")
    public ResponseEntity<BankAccountResponseDto> getBankAccount(
            @Pattern(regexp = "^01\\d{6}$", message = "Invalid account number format")
            @PathVariable final String accountNumber,
            @AuthenticationPrincipal final AuthenticatedUser authUser) {
        return ResponseEntity.ok(
                bankAccountService.getBankAccount(accountNumber, authUser.getId()));
    }

    @PatchMapping(path = "/{accountNumber}")
    public ResponseEntity<BankAccountResponseDto> updateBankAccount(
            @Pattern(regexp = "^01\\d{6}$", message = "Invalid account number format")
            @PathVariable final String accountNumber,
            @Valid @RequestBody final UpdateBankAccountRequestDto bankAccount,
            @AuthenticationPrincipal final AuthenticatedUser authUser) {
        return ResponseEntity.ok(
                bankAccountService.updateBankAccount(accountNumber, authUser.getId(), bankAccount));
    }

    @DeleteMapping(path = "{userId}")
    public ResponseEntity deleteBankAccount(
            @Pattern(regexp = "^01\\d{6}$", message = "Invalid account number format")
            @PathVariable final String accountNumber,
            @AuthenticationPrincipal final AuthenticatedUser authUser) {
        bankAccountService.deleteBankAccount(accountNumber, authUser.getId());

        return ResponseEntity.noContent()
                .build();
    }
}
