package com.denidove.Logistics.enums;

import lombok.Getter;

@Getter

public enum City {
    Moscow("Москва", 91),
    Piter("Санкт-Петербург", 92),
    Sochi("Сочи", 143),
    Saratov("Саратов", 96);

    private String name;
    private Integer id;

    City(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
