package com.denidove.Logistics.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NordWheelCities {
    private String status;
    private Data[] data;

    public NordWheelCities() {
    }

    public NordWheelCities(String status, Data[] data) {
        this.status = status;
        this.data = data;
    }

    @Getter
    @Setter
    public static class Data {
        private Integer id;
        private String name;
        private String type;
        private Integer parent;
    }
}
