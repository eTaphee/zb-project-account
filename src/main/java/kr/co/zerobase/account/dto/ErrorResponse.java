package kr.co.zerobase.account.dto;

import kr.co.zerobase.account.type.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private final int status;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final String path;
}
