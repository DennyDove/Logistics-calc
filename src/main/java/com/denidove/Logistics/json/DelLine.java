package com.denidove.Logistics.json;

public record DelLine(
        String appkey,
        Delivery delivery,
        Payment payment,
        Cargo cargo
) {
    public record Delivery(DeliveryType deliveryType, Derival derival, Arrival arrival) {
        public record DeliveryType(String type) {}
        public record Derival(String variant, String produceDate, Time time, Address address) {}
        public record Arrival(String variant, Address address, Time time, String[] requirements) {}
        public record Address(String search) {}
        public record Time(String worktimeStart, String worktimeEnd) {}
    }

    public record Payment(PaymentCitySearch paymentCitySearch, String type) {
        public record PaymentCitySearch(String search) {}
    }

    public record Cargo(Integer quantity,
                        Float length,
                        Float width,
                        Float height,
                        Float totalVolume,
                        Float totalWeight,
                        Float oversizedWeight,
                        Float oversizedVolume) {}
}

