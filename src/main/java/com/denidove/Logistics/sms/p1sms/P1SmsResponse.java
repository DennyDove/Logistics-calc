package com.denidove.Logistics.sms.p1sms;

import java.util.List;
import java.util.Map;

public record P1SmsResponse(
        String status,
        List<SmsInfo> data,
        String error
) {
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public record SmsInfo(
            String id,
            String phone,
            String status
    ) {}
}

