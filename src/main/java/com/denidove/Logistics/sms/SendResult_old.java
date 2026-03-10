package com.denidove.Logistics.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record SendResult_old(
        boolean success,
        List<String> messageIds,
        String errorMessage,
        Map<String, Object> raw
) {

    @SuppressWarnings("unchecked")
    public static SendResult_old fromMap(Map<String, Object> map) {

        String status = String.valueOf(map.get("status"));

        if ("success".equalsIgnoreCase(status)) {
            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) map.get("data");

            List<String> ids = new ArrayList<>();

            if (data != null) {
                for (Map<String, Object> msg : data) {
                    Object id = msg.get("id");
                    if (id != null) {
                        ids.add(String.valueOf(id));
                    }
                }
            }

            return new SendResult_old(
                    true,
                    ids,
                    null,
                    map
            );
        }

        // fallback — ошибка
        String error =
                map.get("error") != null
                        ? map.get("error").toString()
                        : "Unknown P1SMS error";

        return new SendResult_old(
                false,
                List.of(),
                error,
                map
        );
    }
}

