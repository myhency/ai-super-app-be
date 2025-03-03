package io.hency.aisuperapp.auth.adapter.out.dto;

public record TeamsRefreshTokenRequest(
        String clientId,
        String scope,
        String redirectUri,
        String refreshToken,
        String grantType
) {}
