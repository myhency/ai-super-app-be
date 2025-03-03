package io.hency.aisuperapp.common.error.exception;

import io.hency.aisuperapp.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class ChatNotFoundException extends RuntimeException{
    private final ErrorCode errorCode;

    public ChatNotFoundException(ErrorCode errorCode){
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }
}
