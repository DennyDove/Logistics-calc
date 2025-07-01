package com.denidove.Logistics.json;

import jakarta.persistence.GeneratedValue;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.boot.model.source.spi.DerivedValueSource;

import java.lang.ref.ReferenceQueue;

@Getter
@Setter

public class DelLine {
    private String appkey;
    private Delivery delivery;
    private Payment payment;
    private Cargo cargo;

    public DelLine(String appkey, Delivery delivery, Payment payment, Cargo cargo) {
        this.appkey = appkey;
        this.delivery = delivery;
        this.payment = payment;
        this.cargo = cargo;
    }

    @Getter
    @Setter
    public static class Delivery {
        private DeliveryType deliveryType;
        private Derival derival;
        private Arrival arrival;
        private Packages[] packages;

        public Delivery(DeliveryType deliveryType, Derival derival, Arrival arrival, Packages[] packages) {
            this.deliveryType = deliveryType;
            this.derival = derival;
            this.arrival = arrival;
            this.packages = packages;
        }

        @Getter
        @Setter
        public static class DeliveryType {
            private String type;

            public DeliveryType(String type) {
                this.type = type;
            }
        }

        @Getter
        @Setter
        public static class Derival {
            private String variant;
            private String produceDate;
            private Time time;
            //private String terminalID;
            private Address address;

            public Derival(String variant, String produceDate, String terminalID) {
                this.variant = variant;
                this.produceDate = produceDate;
                //this.terminalID = terminalID;
            }

            public Derival(String variant, String produceDate, Time time, Address address) {
                this.variant = variant;
                this.produceDate = produceDate;
                this.time = time;
                this.address = address;
            }
        }

        @Getter
        @Setter
        public static class Arrival {
            private String variant;
            private Address address;
            private Time time;
            private String[] requirements;

            public Arrival(String variant, Address address, Time time, String[] requirements) {
                this.variant = variant;
                this.address = address;
                this.time = time;
                this.requirements = requirements;
            }

        }

        @Getter
        @Setter
        public static class Address {
            private String search;

            public Address(String search) {
                this.search = search;
            }
        }

        @Getter
        @Setter
        public static class Time {
            private String worktimeStart;
            private String worktimeEnd;

            public Time(String worktimeStart, String worktimeEnd) {
                this.worktimeStart = worktimeStart;
                this.worktimeEnd = worktimeEnd;
            }
        }

        @Getter
        @Setter
        // Возможно это коллекция
        public static class Packages {
            private String uid;
            private Integer count;

            public Packages(String uid, Integer count) {
                this.uid = uid;
                this.count = count;
            }
        }
    }

    @Getter
    @Setter
    public static class Payment {
        private PaymentCitySearch paymentCitySearch;
        private String type;

        public Payment(PaymentCitySearch paymentCitySearch, String type) {
            this.paymentCitySearch = paymentCitySearch;
            this.type = type;
        }

        @Getter
        @Setter
        public static class PaymentCitySearch {
            private String search;

            public PaymentCitySearch(String search) {
                this.search = search;
            }
        }
    }

    @Getter
    @Setter
    public static class Cargo {
        private Integer quantity;
        private Float length;
        private Float width;
        private Float height;
        private Float totalVolume;
        private Float totalWeight;
        private Float oversizedWeight;
        private Float oversizedVolume;

        public Cargo(Integer quantity, Float length, Float width, Float height, Float totalVolume, Float totalWeight,
                     Float oversizedWeight, Float oversizedVolume) {
            this.quantity = quantity;
            this.length = length;
            this.width = width;
            this.height = height;
            this.totalVolume = totalVolume;
            this.totalWeight = totalWeight;
            this.oversizedWeight = oversizedWeight;
            this.oversizedVolume = oversizedVolume;
        }
    }
}

