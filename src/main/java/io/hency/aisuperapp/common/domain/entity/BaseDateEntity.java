package io.hency.aisuperapp.common.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public abstract class BaseDateEntity {
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public BaseDateEntity() {
        ZonedDateTime now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    protected void updateTime() {
        this.updatedAt = ZonedDateTime.now();
    }
}
