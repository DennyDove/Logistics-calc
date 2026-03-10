package com.denidove.Logistics.sms.smsru;

import java.util.Map;

public record SmsRuResponse(
        String status,
        int status_code,
        Map<String, SmsInfo> sms,  // Map<здесь String - номер телефона, SmsInfo> sms
        Double balance
) {
    public record SmsInfo (
            String status,
            int status_code,
            String sms_id,
            String status_text
    ) {
        public boolean isOk() {
            return "OK".equalsIgnoreCase(status);
        }
    }

    public boolean isRequestOk() {
        return "OK".equalsIgnoreCase(status);
    }
}

