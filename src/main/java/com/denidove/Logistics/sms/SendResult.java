package com.denidove.Logistics.sms;

import java.util.List;
import java.util.Map;

public record SendResult(
        boolean success,
        String providerMessageId,
        String errorMessage
) {

    public static SendResult success(String messageId) {
        return new SendResult(true, messageId, null);
    }

    public static SendResult failed(String errorMessage) {
        return new SendResult(false, null, errorMessage);
    }


    /** Пример и описание антипаттерна
     1️⃣ Почему fromMap(Map<String,Object>) — реально плохой код

     Вот ключевые проблемы:

     ❌ 1. Map<String, Object> — потеря контракта
     Map<String, Object> map

     Это:

     нет схемы
     нет типов
     нет гарантий структуры
     всё держится на «я надеюсь, API вернул вот это»

     👉 JSON уже распарсили, но всё равно работаем как с JSON-строкой

     ❌ 2. Небезопасные касты
     (List<Map<String, Object>>) map.get("data");

     Это:

     потенциальный ClassCastException
     IDE не помогает
     рефакторинг опасен

     ❌ 3. Бизнес-логика вперемешку с парсингом

     Метод одновременно:

     парсит ответ API
     решает, что считать успехом
     формирует SendResult

     👉 Это три разные ответственности в одном методе

     ❌ 4. Код невозможно читать «сверху вниз»

     В отличие от твоего mapToSendResult, здесь:

     нет этапов
     нет семантики
     нет «чек-листа»

     */


    // ❌ Структура ответа для сервиса P1SMS - антипаттерн!
    @SuppressWarnings("unchecked")
    public static SendResult fromMap(Map<String, Object> map) {

        String status = String.valueOf(map.get("status"));

        if ("success".equalsIgnoreCase(status)) {

            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) map.get("data");

            if (data != null && !data.isEmpty()) {
                Object phone = data.get(0).get("phone");   // либо можем доставать "id"
                if (phone != null) {
                    return SendResult.success(String.valueOf(phone));
                }
            }

            // success, но без id — редкий, но возможный кейс
            return SendResult.failed("P1SMS success response without message id");
        }

        // ошибка
        String error =
                map.get("error") != null
                        ? map.get("error").toString()
                        : "Unknown P1SMS error";

        return SendResult.failed(error);
    }
}

