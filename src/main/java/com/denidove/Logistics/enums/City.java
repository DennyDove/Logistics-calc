package com.denidove.Logistics.enums;

import lombok.Getter;

@Getter

public enum City {
    Moscow("Москва", 0),
    Piter("Санкт-Петербург", 5.5),
    Vologda("Вологда", 4.0),
    Saratov("Саратов", 7.7);

    private String name;
    private double coeff;

    City(String name, double coeff) {
        this.name = name;
        this.coeff = coeff;
    }
}
