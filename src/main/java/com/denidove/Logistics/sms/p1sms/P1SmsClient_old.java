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
public class P1SmsClient_old {
    private final WebClient webClient;
    private final String apiKey;
    //private final Logger log = LoggerFactory.getLogger(P1SmsClient.class); // закоментили, т.к. навесили аннотацию @Slf4j

    public P1SmsClient_old(@Value("${sms.p1.base-url}") String baseUrl,
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

    public Mono<SendResult> sendSms(String phoneNum, String msg) {
        var smsItem = new SmsItem("char", phoneNum, msg, "VIRTA");

        // ❌ Антипаттерн!
        Map<String, Object> body = new HashMap<>();
        body.put("apiKey", apiKey);
        body.put("sms", List.of(smsItem));
        //if (webhookUrl != null) body.put("webhookUrl", webhookUrl);

        return webClient.post()
                .uri("/apiSms/create") // как в документации: https://admin.p1sms.ru/apiSms/create
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class) // можно создать конкретный POJO для ответа
                .map(SendResult::fromMap)
                .doOnError(err -> log.error("P1SMS send error", err));
    }
}

