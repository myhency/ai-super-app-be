package io.hency.aisuperapp.auth.application.port.in;

import reactor.core.publisher.Mono;

public interface TeamsLoginUseCase {
    Mono<String> createTeamsLoginUrl();
    Mono<String> createOneDriveLoginUrl();
    Mono<String> login(String code, String verificationCode);
}
