package kr.co.zerobase.account.controller;

import javax.validation.Valid;
import kr.co.zerobase.account.aop.ModifyAccountLock;
import kr.co.zerobase.account.dto.UseBalance;
import kr.co.zerobase.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/use")
    @ModifyAccountLock
    public UseBalance.ResponseDto useBalance(
        @RequestBody @Valid UseBalance.RequestDto request) {
        return UseBalance.ResponseDto.from(
            transactionService.useBalance(
                request.getUserId(),
                request.getAccountNumber(),
                request.getAmount())
        );
    }
}
