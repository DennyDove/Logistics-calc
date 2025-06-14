package com.denidove.Logistics.enums;

import lombok.Getter;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public enum TaskStatus {
    InCart,
    InWork("в работе"),
    Done("завершен"),
    Archive("в архиве"),
    Deleted;

    private String title;

    TaskStatus(String title) {
        this.title = title;
    }
}
