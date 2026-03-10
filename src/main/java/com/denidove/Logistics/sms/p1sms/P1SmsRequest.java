package com.denidove.Logistics.sms.p1sms;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"apiKey", "sms", "webhookUrl"}) // Нужно чтобы в json сохранялся порядок полей
public record P1SmsRequest(
        String apiKey,
        List<SmsItem> sms,
        String webhookUrl
) {}
