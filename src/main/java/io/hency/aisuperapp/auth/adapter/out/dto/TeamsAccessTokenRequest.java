package io.hency.aisuperapp.auth.adapter.out.dto;

public record TeamsAccessTokenRequest(
        String clientId,
        String scope,
        String code,
        String redirectUri,
        String grantType
) {}
