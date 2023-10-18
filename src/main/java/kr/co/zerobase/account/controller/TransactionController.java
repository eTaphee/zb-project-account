package kr.co.zerobase.account.controller;

import static kr.co.zerobase.account.type.TransactionType.CANCEL;
import static kr.co.zerobase.account.type.TransactionType.USE;

import javax.validation.Valid;
import kr.co.zerobase.account.aop.ModifyAccountLock;
import kr.co.zerobase.account.dto.CancelBalance;
import kr.co.zerobase.account.dto.GetTransaction;
import kr.co.zerobase.account.dto.UseBalance;
import kr.co.zerobase.account.exception.AccountException;
import kr.co.zerobase.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("use")
    @ModifyAccountLock
    public UseBalance.ResponseDto useBalance(
        @RequestBody @Valid UseBalance.RequestDto request) {
        try {
            return UseBalance.ResponseDto.from(
                transactionService.useBalance(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getAmount())
            );
        } catch (AccountException e) {
            transactionService.saveFailedUseTransaction(
                USE,
                request.getAccountNumber(),
                request.getAmount(),
                e.getErrorCode()
            );

            throw e;
        }
    }

    @PostMapping("/{transactionId}/cancel")
    @ModifyAccountLock
    public CancelBalance.ResponseDto cancelBalance(
        @PathVariable String transactionId,
        @RequestBody @Valid CancelBalance.RequestDto request) {
        try {
            return CancelBalance.ResponseDto.from(
                transactionService.cancelBalance(
                    transactionId,
                    request.getAccountNumber(),
                    request.getAmount())
            );
        } catch (AccountException e) {
            transactionService.saveFailedUseTransaction(
                CANCEL,
                request.getAccountNumber(),
                request.getAmount(),
                e.getErrorCode()
            );

            throw e;
        }
    }

    @GetMapping("{transactionId}")
    public GetTransaction.ResponseDto getTransaction(@PathVariable String transactionId) {
        return GetTransaction.ResponseDto.from(transactionService.queryTransaction(transactionId));
    }
}
