package io.hency.aisuperapp.infrastructure.config.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Getter
public class DatabaseConfig {

    @Value("${spring.data.mysql.port}")
    private int port;
    @Value("${spring.data.mysql.host}")
    private String host;
    @Value("${spring.data.mysql.database}")
    private String database;
    @Value("${spring.data.mysql.username}")
    private String username;
    @Value("${spring.data.mysql.password}")
    private String password;
}
