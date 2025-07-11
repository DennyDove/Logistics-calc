package com.denidove.Logistics.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vozovoz {
    private String object;
    private String action;
    private Params params;

    public Vozovoz(String object, String action, Params params) {
        this.object = object;
        this.action = action;
        this.params = params;
    }

    @Getter
    @Setter
    public static class Params {
        private Cargo cargo;
        private Gateway gateway;

        public Params(Cargo cargo, Gateway gateway) {
            this.cargo = cargo;
            this.gateway = gateway;
        }

        @Getter
        @Setter
        public static class Cargo {
            private Dimension dimension;

            public Cargo(Dimension dimension) {
                this.dimension = dimension;
            }

            @Getter
            @Setter
            public static class Dimension {
                private String quantity;
                private String volume;
                private String weight;
                private Max max;

                public Dimension(String quantity, String volume, String weight, Max max) {

                    this.quantity = quantity;
                    this.volume = volume;
                    this.weight = weight;
                    this.max = max;
                }

                @Getter
                @Setter
                public static class Max {
                    private Float length;
                    private Float height;
                    private Float width;
                    private Float weight;

                    public Max(Float length, Float height, Float width, Float weight) {
                        this.length = length;
                        this.height = height;
                        this.width = width;
                        this.weight = weight;
                    }
                }
            }
        }

        @Getter
        @Setter
        public static class Gateway {
            private Dispatch dispatch;
            private Destination destination;

            public Gateway(Dispatch dispatch, Destination destination) {
                this.dispatch = dispatch;
                this.destination = destination;
            }

            @Getter
            @Setter
            // откуда
            public static class Dispatch {
                private Point point;

                public Dispatch(Point point) {
                    this.point = point;
                }
            }

            @Getter
            @Setter
            // куда
            public static class Destination {
                private Point point;

                public Destination(Point point) {
                    this.point = point;
                }
            }

            @Getter
            @Setter
            public static class Point {
                private String location;
                private String terminal;

                public Point(String location, String terminal) {
                    this.location = location;
                    this.terminal = terminal;
                }
            }
        }

    }
}
