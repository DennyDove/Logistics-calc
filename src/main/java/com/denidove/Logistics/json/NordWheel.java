package com.denidove.Logistics.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NordWheel {
    private String status;
    private Data data;

    public NordWheel() {
    }

    public NordWheel(String status, Data data) {
        this.status = status;
        this.data = data;
    }

    @Getter
    @Setter
    public static class Data {
        private Double total;
        private Integer door;
        private Integer terminal;
        private Double tariff;
        private Integer pick;
        private Integer deliver;
        private Integer days;
    }
}
