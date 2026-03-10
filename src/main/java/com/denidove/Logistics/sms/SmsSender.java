package com.denidove.Logistics.sms;

import com.denidove.Logistics.sms.smsru.SmsRuResponse;
import reactor.core.publisher.Mono;

public interface SmsSender {

    Mono<SendResult> sendSms(String phoneNum, String msg);

}
