package io.hmg.openai.common.error;

import java.util.Date;

public record ErrorResponse(String code, String timestamp) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), new Date().toString());
    }
}
