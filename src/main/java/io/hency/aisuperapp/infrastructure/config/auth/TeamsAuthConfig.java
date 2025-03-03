package io.hency.aisuperapp.infrastructure.config.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Getter
public class TeamsAuthConfig {
    @Value("${domain.server}")
    private String serverUrl;
    @Value("${domain.front-end}")
    private String frontEndUrl;
    @Value("${external.teams.auth-api.url}")
    private String baseAuthUrl;
    @Value("${external.teams.graph-api.url}")
    private String baseGraphUrl;
    @Value("${external.teams.client-secret}")
    private String clientSecret;
    @Value("${external.teams.client-id}")
    private String clientId;
    @Value("${external.teams.tenant-ids}")
    private List<String> tenantIds;
    private final String authUrl = "/organizations/oauth2/v2.0/authorize";
    private final String scope = "openid https://graph.microsoft.com/mail.read offline_access";
    private final String redirectSegment = "/v1/auth/login";

    public Mono<String> generateAuthorizeUrl(
            String baseUrl,
            String clientId,
            String verificationCode,
            String redirectUrl
    ) {
        return Mono.just(
                UriComponentsBuilder.fromHttpUrl(baseUrl + authUrl)
                        .queryParam("client_id", clientId)
                        .queryParam("response_type", "code")
                        .queryParam("redirect_uri", redirectUrl)
                        .queryParam("response_mode", "query")
                        .queryParam("scope", scope)
                        .queryParam("state", verificationCode)
                        .build().toString()
        );
    }
}
