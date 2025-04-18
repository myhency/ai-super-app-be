package io.hency.aisuperapp.infrastructure.config.web.filter;

import java.util.List;

public class WebFilterConstants {
    public static final List<String> AUTH_EXCLUDE_URL_LIST = List.of(
            "/v1/auth/**",
//            "/health-check",
            "/favicon.ico",
            "/webjars/swagger-ui/**",
            "/execute",
            "/clone",
            "/analyze/project",
            "/mcp-test",
            "/mcp/sse",
            "/sse",
            "/api/mcp/sse/**",
            "/test-mcp/**",
            "/test-message/**",
            "/test-sse/**",
            "/sse/**",
            "/message/**"
    );

    public static final String TID = "T-Id";
}
