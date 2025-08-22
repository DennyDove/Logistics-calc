package com.denidove.Logistics.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DellineErr {

    private Metadata metadata;
    private Errors[] errors;

    // Важно добавить данный конструктор без аргументов, иначе будет выдаваться ошибка:
    // Cannot construct instance of `org.example.deserializerJ.DellineErr$Metadata` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
    public DellineErr() {
    }

    public DellineErr(Metadata metadata, Errors[] errors) {
        this.metadata = metadata;
        this.errors = errors;
    }

    @Getter
    @Setter
    public static class Metadata {
        private Integer status;
        private String detail;
        private String generated_at;

        // Важно добавить данный конструктор без аргументов, иначе будет выдаваться ошибка:
        // Cannot construct instance of `org.example.deserializerJ.DellineErr$Metadata` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
        public Metadata() {
        }

        public Metadata(Integer status, String detail, String generated_at) {
            this.status = status;
            this.detail = detail;
            this.generated_at = generated_at;
        }
    }

    @Getter
    @Setter
    public static class Errors {
        private Integer code;
        private String title;
        private String detail;
        private String link;
        private String[] fields;

        // Важно добавить данный конструктор без аргументов, иначе будет выдаваться ошибка:
        // Cannot construct instance of `org.example.deserializerJ.DellineErr$Metadata` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
        public Errors() {
        }

        public Errors(Integer code, String title, String detail, String link, String[] fields) {
            this.code = code;
            this.title = title;
            this.detail = detail;
            this.link = link;
            this.fields = fields;
        }
    }
}
