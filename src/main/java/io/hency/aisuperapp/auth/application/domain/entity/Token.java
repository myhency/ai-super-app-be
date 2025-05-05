package io.hency.aisuperapp.auth.application.domain.entity;

import io.hency.aisuperapp.auth.application.domain.vo.ApplicationType;

import java.time.ZonedDateTime;

public record Token(
        String accessToken,
        String refreshToken,
        ZonedDateTime expiredAt,
        ApplicationType applicationType
) {
}
