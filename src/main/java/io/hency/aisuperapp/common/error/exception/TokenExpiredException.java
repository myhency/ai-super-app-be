package io.hency.aisuperapp.common.error.exception;

import io.hency.aisuperapp.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class TokenExpiredException extends RuntimeException {
    private final ErrorCode errorCode;

    public TokenExpiredException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }
}
