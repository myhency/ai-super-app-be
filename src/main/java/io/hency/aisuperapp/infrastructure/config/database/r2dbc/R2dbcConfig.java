package io.hency.aisuperapp.infrastructure.config.database.r2dbc;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.infrastructure.config.database.DatabaseConfig;
import io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.read.UlidReadConverter;
import io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.read.ZonedDateTimeReadConverter;
import io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.write.UlidWriteConverter;
import io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.write.ZonedDateTimeWriteConverter;
import io.hency.aisuperapp.infrastructure.config.web.context.UserContextHolder;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "io.hmg.aisuperapp")
@EnableR2dbcAuditing
@EntityScan(basePackages = "io.hmg.aisuperapp.infrastructure")
@RequiredArgsConstructor
@ConditionalOnClass(DatabaseConfig.class)
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    private final DatabaseConfig databaseConfig;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .serverZoneId(ZoneId.of("Asia/Seoul"))
                .host(databaseConfig.getHost())
                .port(databaseConfig.getPort())
                .username(databaseConfig.getUsername())
                .password(databaseConfig.getPassword())
                .database(databaseConfig.getDatabase())
                .build();
        return MySqlConnectionFactory.from(configuration);
    }

    @Bean
    public ReactiveAuditorAware<String> auditorAware() {
        return () -> UserContextHolder.getUserMono().map(User::id);
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(DatabaseClient databaseClient) {
        var dialect = DialectResolver.getDialect(databaseClient.getConnectionFactory());
        var converters = new ArrayList<>(dialect.getConverters());
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS);

        return new R2dbcCustomConversions(
                CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder(), converters),
                getCustomConverters()
        );
    }

    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
                new UlidReadConverter(),
                new UlidWriteConverter(),
                new ZonedDateTimeReadConverter(),
                new ZonedDateTimeWriteConverter()
        );
    }
}
