package com.denidove.Logistics.sms.p1sms;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"channel", "phone", "text", "sender"}) // Нужно чтобы в json сохранялся порядок полей
public record SmsItem (
        String channel,
        String phone,
        String text,
        String sender
) {}
