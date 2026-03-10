package com.denidove.Logistics.sms.smsru;

import com.denidove.Logistics.sms.SendResult;
import com.denidove.Logistics.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;


// Адаптер под конкретный сервис SMS.RU

@Service
@Slf4j
@ConditionalOnProperty(  // Так как две реализации, то эта аннотация нужна для выбора конкретной реализации, но
        prefix = "sms",  // более простой вариант использовать @Primary или @Qualifier
        name = "provider",
        havingValue = "smsru"
)
public class SmsRuSender implements SmsSender {

    private final SmsRuClient smsRuClient;

    public SmsRuSender(SmsRuClient smsRuClient) {
        this.smsRuClient = smsRuClient;
    }

    @Override
    public Mono<SendResult> sendSms(String phone, String message) {
        return smsRuClient.sendSms(phone, message)
                .map(response -> mapToSendResult(phone, response))
                .onErrorResume(ex ->
                        Mono.just(SendResult.failed("SMS.ru error: " + ex.getMessage()))
                );
    }


    private SendResult mapToSendResult(String phone, SmsRuResponse response) {

        // 1️⃣ Базовая валидация
        if (response == null) {
            return SendResult.failed("Empty response from SMS.ru");
        }

        log.info("Raw response {}", response);

        // 2️⃣ Проверка статуса запроса
        if (!"OK".equalsIgnoreCase(response.status())) {
            return SendResult.failed(
                    "SMS.ru request failed: " + response.status()
            );
        }

        // 3️⃣ Проверка блока sms
        Map<String, SmsRuResponse.SmsInfo> smsMap = response.sms();
        if (smsMap == null || !smsMap.containsKey(phone)) {
            return SendResult.failed(
                    "SMS.ru response does not contain result for phone " + phone
            );
        }

        SmsRuResponse.SmsInfo smsInfo = smsMap.get(phone);

        // 4️⃣ Проверка конкретной SMS
        if (smsInfo == null) {
            return SendResult.failed(
                    "SMS.ru response does not contain info for phone: " + phone
            );
        }

        if (!"OK".equalsIgnoreCase(smsInfo.status())) {
            return SendResult.failed(
                    "SMS.ru send error: " + smsInfo.status_text()
            );
        }
        // 5️⃣ УСПЕХ
        return SendResult.success(smsInfo.sms_id());
    }
}
