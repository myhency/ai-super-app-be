package io.hency.aisuperapp.features.user.adapter.out.dto;

public record TeamsUser(
        String id,
        String mail,
        String userPrincipalName,
        String displayName
) {
}
