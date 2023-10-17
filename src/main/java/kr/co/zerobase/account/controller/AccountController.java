package kr.co.zerobase.account.controller;

import javax.validation.Valid;
import kr.co.zerobase.account.aop.CreateAccountLock;
import kr.co.zerobase.account.dto.CreateAccount;
import kr.co.zerobase.account.dto.DeleteAccount;
import kr.co.zerobase.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(
            accountService.createAccount(
                request.getUserId(),
                request.getInitialBalance())
        );
    }

    @DeleteMapping("{accountNumber}")
    public DeleteAccount.Response deleteAccount(
        @PathVariable String accountNumber,
        @RequestBody @Valid DeleteAccount.Request request) {
        // TODO: DeleteAccount.Request 필드가 하나인 경우 @JsonCreator 없이는 예외가 발생하는 이유는?
        // TODO: DELETE 메서드 request body가 오는 것이 맞는지?
        // TODO: 사용자를 식별 할 수 있는 토큰 없이 아래와 같은 요청 형식은 어떤지?
        // DELETE /accounts/{accountNumber}
        // {
        //   "userId": 1
        // }

        return DeleteAccount.Response.from(
            accountService.deleteAccount(
                request.getUserId(),
                accountNumber
            )
        );
    }
}
