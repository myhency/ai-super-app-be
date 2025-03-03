package io.hency.aisuperapp.auth.domain.entity;

import io.hency.aisuperapp.auth.constant.ApplicationType;

import java.time.ZonedDateTime;

public record Token(
        String accessToken,
        String refreshToken,
        ZonedDateTime expiredAt,
        ApplicationType applicationType
) {
}
