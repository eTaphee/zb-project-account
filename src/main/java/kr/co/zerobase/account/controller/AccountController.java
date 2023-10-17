package kr.co.zerobase.account.controller;

import javax.validation.Valid;
import kr.co.zerobase.account.aop.CreateAccountLock;
import kr.co.zerobase.account.dto.CreateAccount;
import kr.co.zerobase.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @CreateAccountLock
    public CreateAccount.Response createAccount(@Valid @RequestBody CreateAccount.Request request) {
        return CreateAccount.Response.from(
            accountService.createAccount(
                request.getUserId(),
                request.getInitialBalance())
        );
    }
}
