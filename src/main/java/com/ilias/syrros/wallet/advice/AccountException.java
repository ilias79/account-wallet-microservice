package com.ilias.syrros.wallet.advice;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountException extends RuntimeException{

    private String detail;
    private int code;

    public AccountException(){}

    public AccountException(int code, String detail) {
        this.detail = detail;
        this.code = code;
    }
}
