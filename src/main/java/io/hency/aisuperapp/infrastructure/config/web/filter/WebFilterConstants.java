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
            "/analyze/project"
    );

    public static final String TID = "T-Id";
}
