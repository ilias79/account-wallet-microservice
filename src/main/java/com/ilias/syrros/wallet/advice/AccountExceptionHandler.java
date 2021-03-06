package com.ilias.syrros.wallet.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class AccountExceptionHandler {

    @ExceptionHandler(value = { Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse unknownException(Exception ex) {

        log.info("inside unknownException");
        log.error(ex.getMessage());

        return ErrorResponse.builder()
                .exception(ex.getLocalizedMessage())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

    @ExceptionHandler(value = { AccountException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse generalException(AccountException ex) {

        log.info("inside generalException");
        log.error(ex.getDetail());

        return ErrorResponse.builder()
                .exception(ex.getDetail())
                .code(ex.getCode())
                .build();
    }

    @ExceptionHandler(value = { RateLimiterException.class})
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ResponseBody
    public ErrorResponse rateLimitException(RateLimiterException ex) {

        log.info("inside rateLimitException");
        log.error(ex.getDetail());

        return ErrorResponse.builder()
                .exception(ex.getDetail())
                .code(ex.getCode())
                .build();
    }

}
