package com.denidove.Logistics.sms.p1sms;

import com.denidove.Logistics.sms.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class P1SmsClient {
    private final WebClient webClient;
    private final String apiKey;
    //private final Logger log = LoggerFactory.getLogger(P1SmsClient.class); // закоментили, т.к. навесили аннотацию @Slf4j

    public P1SmsClient(@Value("${sms.p1.base-url}") String baseUrl,
                       @Value("${sms.p1.api-key}") String apiKey,
                       @Value("${sms.p1.timeout-ms}") int timeoutMs,
                       WebClient.Builder webClientBuilder) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs))
                ))
                .build();
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
     */

    /**
     * Отправка SMS через P1SMS
     * ❗ Возвращает ТОЛЬКО ответ провайдера
     */
    public Mono<P1SmsResponse> sendSms(String phoneNum, String msg) {

        SmsItem smsItem = new SmsItem(
                "char",     // channel
                phoneNum,
                msg,
                "VIRTA"     // sender
        );

        P1SmsRequest request = new P1SmsRequest(
                apiKey,
                List.of(smsItem),
                null        // webhookUrl (пока не используем)
        );

        return webClient.post()
                .uri("/apiSms/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(P1SmsResponse.class)
                .doOnError(err ->
                        log.error("P1SMS send error, phone={}", phoneNum, err)
                );
    }
}

