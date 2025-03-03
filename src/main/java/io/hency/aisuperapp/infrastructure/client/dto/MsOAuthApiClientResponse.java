package io.hency.aisuperapp.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hency.aisuperapp.auth.constant.ApplicationType;
import io.hency.aisuperapp.auth.domain.entity.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

public class MsOAuthApiClientResponse {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetAccessTokenResponse {
        @JsonProperty("token_type")
        private String tokenType; // Bearer
        private String scope;
        @JsonProperty("expires_in")
        private Long expiresIn;
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
        @JsonProperty("id_token")
        private String idToken;

        public Token toToken(ApplicationType applicationType) {
            return new Token(
                    this.accessToken,
                    this.refreshToken,
                    ZonedDateTime.now().plusSeconds(this.expiresIn),
                    applicationType
            );
        }
    }


}
