package io.hency.aisuperapp.common.adapter.in;

import io.hency.aisuperapp.common.adapter.in.dto.AnalyzeGitlabProjectRequest;
import io.hency.aisuperapp.common.adapter.in.dto.CloneRequest;
import io.hency.aisuperapp.features.user.domain.entity.User;
import io.hency.aisuperapp.infrastructure.client.OpenAIApiClient;
import io.hency.aisuperapp.infrastructure.client.dto.OpenAIApiClientRequest;
import io.hency.aisuperapp.infrastructure.client.dto.OpenAIApiClientResponse;
import io.hency.aisuperapp.infrastructure.config.azure.openai.AzureOpenAIConfig;
import io.hency.aisuperapp.infrastructure.config.web.context.RequestContextHolder;
import io.hency.aisuperapp.infrastructure.config.web.context.UserContextHolder;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommonController {
    private final OpenAIApiClient openAIApiClient;
    private final AzureOpenAIConfig azureOpenAIConfig;

    @GetMapping("/test-mcp")
    public Mono<String> mcpTest() {
        var client = getClient();
        return client.initialize()
                .flatMap(initResult -> {
                    log.info("클라이언트 초기화 완료: {}", initResult);
                    return client.listTools();
                })
                .flatMap(toolsResult -> {
                    log.info("사용 가능한 도구: {}", toolsResult.tools());
                    if (toolsResult.tools().isEmpty()) {
                        return Mono.just("사용 가능한 도구가 없습니다");
                    }

                    // 도구 호출
                    return client.callTool(new McpSchema.CallToolRequest(
                                    "calculation",  // 도구 이름
                                    Map.of("operation", "add", "a", 2, "b", 3)
                            ))
                            .map(callResult -> {
                                if (callResult.content() != null && !callResult.content().isEmpty()) {
                                    McpSchema.Content firstContent = callResult.content().get(0);

                                    // Content 타입 확인 및 변환
                                    if (firstContent instanceof McpSchema.TextContent textContent) {
                                        return "도구 호출 성공: " + textContent.text();
                                    } else {
                                        return "도구 호출 성공: " + firstContent.toString();
                                    }
                                }
                                return "도구 호출 성공 (내용 없음)";
                            });
                })
                .onErrorResume(e -> {
                    log.error("MCP 테스트 오류", e);
                    return Mono.just("오류: " + e.getMessage());
                })
                .doOnSuccess(result -> {
                    client.close();
                });
    }

    private McpAsyncClient getClient() {
        // 가장 단순한 경로 구조 사용
        var transport = new WebFluxSseClientTransport(WebClient.builder()
                .baseUrl("http://localhost:8080")
        );

        return McpClient
                .async(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .build())
                .toolsChangeConsumer(tools -> {
                    log.info("도구 업데이트: {}", tools);
                    return Mono.empty();
                })
                .build();
    }


    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> handleFaviconRequest() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health-check")
    public Mono<ResponseEntity<String>> healthCheck() {

        return Mono.zip(
                        UserContextHolder.getUserMono(),
                        RequestContextHolder.getRequest()
                )
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    ServerHttpRequest request = tuple.getT2();

                    log.info("User: {}", user.email());
                    log.info("Request: {}", request.getPath());

                    return Mono.just(ResponseEntity.ok("OK"));
                });
    }

    @GetMapping(value = "/test/open-ai-api-client/send-message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<OpenAIApiClientResponse>> testSendMessage(@RequestParam String messageText) {
        AzureOpenAIConfig.ApiResource resource = azureOpenAIConfig.getAzureSubscriptions().get(0).getResources().get(0);

        // Build the content for the chat message
        var content1 = OpenAIApiClientRequest.ChatRequestMessage.Content.builder()
                .type("text")
                .text(messageText)
                .build();

        var imageUrl = OpenAIApiClientRequest.ChatRequestMessage.Content.ImageUrl.builder()
                .url("data:image/jpeg;base64,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")
                .build();

        var content2 = OpenAIApiClientRequest.ChatRequestMessage.Content.builder()
                .type("image_url")
                .imageUrl(imageUrl)
                .build();

        var chatMessage = OpenAIApiClientRequest.ChatRequestMessage.builder()
                .role("user")
                .content(List.of(content1, content2))
                .build();

        var messages = List.of(chatMessage);

        log.debug("Sending message with text: {}", messageText);

        // Fetch the response as Flux
        Flux<OpenAIApiClientResponse> response = openAIApiClient.sendMessage(messages, resource)
                .doOnError(error -> log.error("Error during OpenAI API call", error))
                .onErrorResume(error -> {
                    // Handle any errors gracefully
                    log.error("Handling error gracefully: {}", error.getMessage());
                    return Flux.empty();
                });

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(response);
    }

    @GetMapping("/execute")
    public ResponseEntity<String> executeRepomix(@RequestParam("url") String url) {
        try {
            File workingDirectory = new File("/Users/james/Developer/repomix-test");
            ProcessBuilder processBuilder = new ProcessBuilder("npx", "repomix", "--remote", url);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            int exitCode = process.waitFor();
            return ResponseEntity.ok().body("Exit code: " + exitCode + "\nOutput: " + output);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing command: " + e.getMessage());
        }
    }

    @PostMapping("/clone")
    public ResponseEntity<String> cloneRepository(@RequestBody CloneRequest cloneRequest) {
        String token = cloneRequest.getToken();
        String repoUrl = cloneRequest.getRepoUrl();
        String targetDir = cloneRequest.getTargetDir();

        File cloneDirectory = new File(targetDir);
        if (cloneDirectory.exists()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("클론할 디렉토리가 이미 존재합니다: " + targetDir);
        }

        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(cloneDirectory)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("oauth2", token))
                    .call();
            return ResponseEntity.ok("리포지토리가 " + targetDir + " 에 클론되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("클론 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/analyze/project")
    public ResponseEntity<?> analyzeGitlabProject(@Valid @RequestBody AnalyzeGitlabProjectRequest request) {
        Flux<String> responseFlux = Flux.create(sink -> {
            String BASE_DIR = "/Users/james/Developer/repomix-test";
            String token = request.getToken();
            String repoUrl = request.getRepoUrl();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomText = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            File targetDirectory = new File(BASE_DIR + File.separator + today + File.separator + randomText);

            if (targetDirectory.exists()) {
                deleteDirectory(targetDirectory);
            }

            if (!targetDirectory.mkdirs()) {
                sink.error(new RuntimeException("디렉토리 생성 실패: " + targetDirectory.getAbsolutePath()));
                return;
            }

            try {
                // GitLab HTTP 인증 시 사용자명은 보통 "oauth2" (필요에 따라 변경)
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(targetDirectory)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider("oauth2", token))
                        .call();
                sink.next("repository clone 이 끝났습니다.");

                ProcessBuilder processBuilder = new ProcessBuilder("npx", "repomix", "--style", "markdown");
                processBuilder.directory(targetDirectory);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
                int exitCode = process.waitFor();

                sink.next("프로젝트가 패키징 되었습니다.");

                sink.next(output);

                sink.next(String.valueOf(exitCode));

                File repomixOutputFile = new File(targetDirectory, "repomix-output.md");
                int exactTokenCount = 0;
                if (repomixOutputFile.exists()) {
                    String repomixOutput = Files.readString(repomixOutputFile.toPath());
                    exactTokenCount = getExactTokenCount(repomixOutput);
                }

                sink.next("프로젝트 패키징된 내용의 전체 토큰은 " + exactTokenCount + " 입니다.");
                sink.complete();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                sink.error(new RuntimeException("명령 실행 중 오류 발생: " + e.getMessage()));
            } catch (Exception e) {
                sink.error(new RuntimeException("클론 중 오류 발생: " + e.getMessage()));
            }
        });
        return ResponseEntity.ok(responseFlux);
    }

    @SneakyThrows
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    @SneakyThrows
    private int getExactTokenCount(String text) {
        // Python 스크립트 실행 (count_tokens.py 파일이 동일한 경로에 있다고 가정)
        ProcessBuilder pb = new ProcessBuilder("python3", "/Users/james/Developer/repomix-test/count_tokens.py", "gpt-4");
        Process process = pb.start();

        // repomix-output.txt의 내용을 Python 프로세스의 STDIN으로 전달
        try (OutputStream os = process.getOutputStream()) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String result = reader.lines().collect(Collectors.joining("\n"));
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Python 스크립트 실행 실패, exit code: " + exitCode);
        }
        return Integer.parseInt(result.trim());
    }
}
