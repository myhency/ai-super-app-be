package io.hency.aisuperapp.auth.adapter.in;

import io.hency.aisuperapp.auth.adapter.in.dto.TeamsLoginUrlResponse;
import io.hency.aisuperapp.auth.application.port.in.TeamsLoginUseCase;
import io.hency.aisuperapp.common.util.HttpUtils;
import io.hency.aisuperapp.infrastructure.config.auth.TeamsAuthConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Tag(name = "TeamsAuth", description = "Microsoft Teams 인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class TeamsAuthController {
    private final TeamsLoginUseCase teamsLoginUseCase;
    private final TeamsAuthConfig teamsAuthConfig;

    /**
     * Microsoft Teams 인증 URL 조회
     */
    @Operation(
            summary = "Teams 로그인 URL 조회",
            description = "Microsoft Teams 인증을 위한 로그인 URL을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 URL 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeamsLoginUrlResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/teams/login-url")
    public Mono<TeamsLoginUrlResponse> retrieveTeamsLoginUrl() {
        return teamsLoginUseCase.createTeamsLoginUrl()
                .map(TeamsLoginUrlResponse::new);
    }

    /**
     * MS 인증 콜백 처리
     */
    @Operation(
            summary = "Microsoft 인증 콜백",
            description = "Microsoft 인증 후 리다이렉트되는 콜백 엔드포인트. Authorization Code를 받아 로그인 처리한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "로그인 성공 후 클라이언트를 리다이렉트"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authorization 문제"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/login")
    public Mono<Void> loginFromBrowser(
            String code,
            String state,
            ServerHttpResponse response
    ) {
        String frontEndUrl = teamsAuthConfig.getFrontEndUrl();

        return teamsLoginUseCase.login(code, state)
                .flatMap(accessToken ->
                    HttpUtils.redirect(UriComponentsBuilder
                            .fromUriString(frontEndUrl + "/login-redirect#accessToken={accessToken}")
                            .build(accessToken), response)
                )
                .onErrorResume(error -> {
                    log.error("Login failed", error);
                    return HttpUtils.redirect(UriComponentsBuilder
                            .fromUriString(frontEndUrl + "/error")
                            .build().toUri(), response);
                });

    }
}
