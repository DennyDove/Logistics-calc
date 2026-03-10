package com.denidove.Logistics.sms.p1sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SmsWebhookController.class);

    @PostMapping("/status")
    public ResponseEntity<Void> handleStatus(@RequestBody P1SmsResponse dto) {
        log.info("SMS {} status: {}", dto.data(), dto.status());
        return ResponseEntity.ok().build();
    }
}

