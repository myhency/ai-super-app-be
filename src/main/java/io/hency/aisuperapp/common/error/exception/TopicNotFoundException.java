package io.hency.aisuperapp.common.error.exception;

import io.hency.aisuperapp.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class TopicNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public TopicNotFoundException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }
}
