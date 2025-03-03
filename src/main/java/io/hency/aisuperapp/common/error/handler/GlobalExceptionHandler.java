package io.hency.aisuperapp.common.error.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.ErrorResponse;
import io.hency.aisuperapp.common.error.exception.InternalServerErrorException;
import io.hency.aisuperapp.common.error.exception.TopicNotFoundException;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Order(-2)
@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return handleException(exchange, ex);
    }

    private Mono<Void> handleException(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 별도 함수로 분리된 로직
        ErrorAttributes errorAttributes = getErrorAttributes(ex);

        // 추출된 결과에서 HttpStatus 및 ErrorResponse 적용
        response.setStatusCode(errorAttributes.status());

        try {
            var dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorAttributes.response()));
            return response.writeWith(Mono.just(dataBuffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * Throwable(에러) 타입에 따라 적절한 HttpStatus와 ErrorResponse를 반환하는 함수
     */
    private ErrorAttributes getErrorAttributes(Throwable ex) {
        if (ex instanceof UnauthorizedException ue) {
            ErrorCode errorCode = ue.getErrorCode();
            log.error("[UnauthorizedException] code={}, desc={}",
                    errorCode.getCode(), errorCode.getDescription(), ex);

            return new ErrorAttributes(
                    errorCode.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(errorCode.getCode()))
            );
        } else if (ex instanceof WebExchangeBindException bindException) {
            String errorMessage = bindException.getBindingResult()
                    .getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            log.debug("[ValidationException] desc={}", errorMessage, bindException);
            return new ErrorAttributes(
                    ErrorCode.H400A.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(ErrorCode.H400A.getCode()))
            );
        } else if (ex instanceof InternalServerErrorException ise) {
            ErrorCode errorCode = ise.getErrorCode();
            log.error("[InternalServerErrorException] code={}, desc={}",
                    errorCode.getCode(), errorCode.getDescription(), ex);

            return new ErrorAttributes(
                    errorCode.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(errorCode.getCode()))
            );
        } else if (ex instanceof IllegalArgumentException iae) {
            log.debug("[IllegalArgumentException] desc={}", iae.getMessage(), iae);
            return new ErrorAttributes(
                    ErrorCode.H400A.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(ErrorCode.H400A.getCode()))
            );
        } else if (ex instanceof TopicNotFoundException tnfe) {
            log.debug("[TopicNotFoundException] desc={}", tnfe.getMessage(), tnfe);
            return new ErrorAttributes(
                    ErrorCode.H400A.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(ErrorCode.H400A.getCode()))
            );
        } else {
            ErrorCode defaultCode = ErrorCode.H500A;
            log.error("[Exception] code={}, desc={}",
                    defaultCode.getCode(), defaultCode.getDescription(), ex);

            return new ErrorAttributes(
                    defaultCode.getHttpStatus(),
                    ErrorResponse.of(ErrorCode.valueOf(defaultCode.getCode()))
            );
        }
    }

    /**
     * HttpStatus와 ErrorResponse를 함께 담기 위한 내부 레코드(또는 클래스)
     */
    private record ErrorAttributes(HttpStatus status, ErrorResponse response) {
    }
}
