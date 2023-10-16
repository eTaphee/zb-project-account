package kr.co.zerobase.account.exception;

import kr.co.zerobase.account.type.ErrorCode;
import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {

    private final int status;
    private final ErrorCode errorCode;
    private final String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
