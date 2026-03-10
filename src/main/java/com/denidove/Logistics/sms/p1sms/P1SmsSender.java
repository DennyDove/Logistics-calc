package com.denidove.Logistics.sms.p1sms;

import com.denidove.Logistics.sms.SendResult;
import com.denidove.Logistics.sms.SmsSender;
import com.denidove.Logistics.sms.smsru.SmsRuResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


// Адаптер под конкретный сервис P1SMS - сервис рабочий --> sms реально отправляется
@Service
@ConditionalOnProperty(  // Так как две реализации, то эта аннотация нужна для выбора конкретной реализации, но
        prefix = "sms",  // более простой вариант использовать @Primary или @Qualifier
        name = "provider",
        havingValue = "p1"
)
public class P1SmsSender implements SmsSender {

    private final P1SmsClient smsClient;

    public P1SmsSender(P1SmsClient smsClient) {
        this.smsClient = smsClient;
    }

    @Override
    public Mono<SendResult> sendSms(String phone, String message) {
        return smsClient.sendSms(phone, message)
                .map(response -> mapToSendResult(phone, response))
                .onErrorResume(ex ->
                        Mono.just(SendResult.failed("P1SMS error: " + ex.getMessage()))
                );
    }

    private SendResult mapToSendResult(String phone, P1SmsResponse response) {

        if (response == null) {
            return SendResult.failed("Empty response from P1SMS");
        }

        if (!response.isSuccess()) {
            return SendResult.failed(
                    response.error() != null ? response.error() : "P1SMS request failed"
            );
        }

        if (response.data() == null || response.data().isEmpty()) {
            return SendResult.failed("P1SMS response contains no messages");
        }

        return response.data().stream()
                .filter(msg -> phone.equals(msg.phone()))
                .findFirst()
                .map(msg -> SendResult.success(msg.id()))
                .orElseGet(() ->
                        SendResult.failed("No message result for phone " + phone)
                );
    }
}
