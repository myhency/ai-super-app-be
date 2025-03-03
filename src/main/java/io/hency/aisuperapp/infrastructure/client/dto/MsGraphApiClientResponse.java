package io.hency.aisuperapp.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MsGraphApiClientResponse {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetTeamsUserResponse {
        private String id;
        private String mail;
        private String userPrincipalName;
        private String displayName;
    }
}
