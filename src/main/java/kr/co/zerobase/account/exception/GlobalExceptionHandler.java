package kr.co.zerobase.account.exception;

import static kr.co.zerobase.account.type.ErrorCode.INTERNAL_SERVER_ERROR;
import static kr.co.zerobase.account.type.ErrorCode.INVALID_REQUEST;

import javax.servlet.http.HttpServletRequest;
import kr.co.zerobase.account.dto.ErrorResponseDto;
import kr.co.zerobase.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountException(HttpServletRequest req,
        AccountException e) {
        log.error("{} is occurred.", e.getErrorCode());
        return getErrorResponseResponseEntity(req, e.getErrorCode(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountException(HttpServletRequest req,
        MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occurred.", e);
        return getErrorResponseResponseEntity(req, INVALID_REQUEST,
            e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(HttpServletRequest req, Exception e) {
        log.error("Exception is occurred.", e);
        return getErrorResponseResponseEntity(req, INTERNAL_SERVER_ERROR, null);
    }

    private static ResponseEntity<ErrorResponseDto> getErrorResponseResponseEntity(
        HttpServletRequest request,
        ErrorCode errorCode, String message) {
        return new ResponseEntity<>(
            ErrorResponseDto.builder()
                .status(errorCode.getStatus())
                .errorCode(errorCode)
                .errorMessage((message == null) ? errorCode.getDescription() : message)
                .path(request.getRequestURI())
                .build(),
            HttpStatus.valueOf(errorCode.getStatus()));
    }
}
