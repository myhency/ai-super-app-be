package io.hmg.claude.messages.infrastructure.service;

import io.hmg.claude.messages.infrastructure.config.AwsBedrockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSignatureService {

    private final AwsBedrockProperties awsBedrockProperties;
    private final Aws4Signer signer = Aws4Signer.create();

    public Mono<Map<String, String>> signRequest(String uri, String body, String region) {
        return Mono.fromCallable(() -> {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    awsBedrockProperties.getCredentials().getAccessKeyId(),
                    awsBedrockProperties.getCredentials().getSecretAccessKey()
            );

            SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                    .method(SdkHttpMethod.POST)
                    .uri(URI.create("https://bedrock-runtime." + region + ".amazonaws.com" + uri))
                    .putHeader("Content-Type", "application/json")
                    .contentStreamProvider(() -> new ByteArrayInputStream(body.getBytes()))
                    .build();

            Aws4SignerParams signerParams = Aws4SignerParams.builder()
                    .awsCredentials(credentials)
                    .signingName("bedrock")
                    .signingRegion(Region.of(region))
                    .build();

            SdkHttpFullRequest signedRequest = signer.sign(httpRequest, signerParams);

            Map<String, String> headers = new HashMap<>();
            signedRequest.firstMatchingHeader("Authorization")
                    .ifPresent(value -> headers.put("Authorization", value));
            signedRequest.firstMatchingHeader("X-Amz-Date")
                    .ifPresent(value -> headers.put("X-Amz-Date", value));
            signedRequest.firstMatchingHeader("X-Amz-Security-Token")
                    .ifPresent(value -> headers.put("X-Amz-Security-Token", value));

            return headers;
        });
    }
}
