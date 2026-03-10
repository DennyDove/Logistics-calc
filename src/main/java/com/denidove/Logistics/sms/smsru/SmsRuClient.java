package com.denidove.Logistics.sms.smsru;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class SmsRuClient {

    private final WebClient webClient;
    private final String apiId;
    private final String from;

    public SmsRuClient(
            @Value("${sms.smsru.base-url}") String baseUrl,
            @Value("${sms.smsru.api-id}") String apiId,
            @Value("${sms.smsru.from}") String from,
            @Value("${sms.smsru.timeout-ms}") int timeoutMs,
            WebClient.Builder builder
    ) {
        this.apiId = apiId;
        this.from = from;

        this.webClient = builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMillis(timeoutMs))
                ))
                .build();
    }

    public Mono<SmsRuResponse> sendSms(String phone, String message) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sms/send")
                        .queryParam("api_id", apiId)
                        .queryParam("to", phone)
                        .queryParam("msg", message)
                        .queryParam("json", 1)
                        .build())
                .retrieve()
                .bodyToMono(SmsRuResponse.class);
    }
}

