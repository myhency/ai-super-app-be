package io.hency.aisuperapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;

@SpringBootApplication
public class AiSuperAppApplication {
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    public static void main(String[] args) {
        SpringApplication.run(AiSuperAppApplication.class, args);
    }

}
