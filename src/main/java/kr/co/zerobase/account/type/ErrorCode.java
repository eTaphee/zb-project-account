package kr.co.zerobase.account.type;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류가 발생했습니다."),
    INVALID_REQUEST(BAD_REQUEST.value(), "잘못된 요청입니다."),

    TRANSACTION_LOCK(HttpStatus.INTERNAL_SERVER_ERROR.value(), null),
    CREATE_ACCOUNT_TRANSACTION_LOCK(HttpStatus.INTERNAL_SERVER_ERROR.value(), "다른 계좌가 생성 중입니다."),

    ACCOUNT_NUMBER_ALREADY_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR.value(), "이미 존재하는 계좌번호입니다."),
    USER_NOT_FOUND(NOT_FOUND.value(), "사용자가 없습니다."),
    MAX_ACCOUNT_PER_USER(BAD_REQUEST.value(), "사용자 최대 계좌 개수를 초과할 수 없습니다."),

    ACCOUNT_NOT_FOUND(NOT_FOUND.value(), "계좌가 없습니다."),
    USER_ACCOUNT_UN_MATCH(FORBIDDEN.value(), "사용자와 계좌의 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED(BAD_REQUEST.value(), "계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY(BAD_REQUEST.value(), "잔액이 있는 계좌는 해지할 수 없습니다.");

    private final int status;
    private final String description;
}
