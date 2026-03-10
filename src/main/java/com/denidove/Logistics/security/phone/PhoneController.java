package com.denidove.Logistics.security.phone;

import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.sms.SmsSender;
import com.denidove.Logistics.sms.p1sms.SmsItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PhoneController {

    private final UserRedisService redisService;
    private final SimpleMailService mailService;
    private final SmsSender smsSender;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(PhoneController.class);

    @PostMapping("/api/auth/phone-send-code")
    public void phoneSendCode(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String phone = request.getParameter("phone");

            if (phone == null || phone.isBlank()) {
                sendError(response, "PHONE_REQUIRED");
                return;
            }

            // Генерируем код
            //String code = smsService.generateVerificationCode();
            String code = RandomString.make(6);

            // Сохраняем в "pending" состояние (в redis)
            redisService.startPendingAuth(phone, code);

            // Отправляем SMS

            //toDO - DONE
            //smsService.sendSms(phone, code);

            /** Временно отключили отправку sms
            /*smsSender.sendSms(phone, code).subscribe(
                    result -> log.info("SmsService response: {}", result),
                    error -> log.error("SmsService error", error)); //  для webHook: "/api/sms/status").subscribe();
             */

            mailService.sendPhoneEmail(phone, code);

            // Для будущего REST-режима — отдаем JSON
            sendOk(response, "CODE_SENT");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "SMS_SEND_ERROR");
        }
    }

    // =============== helpers ===============

    private void sendOk(HttpServletResponse response, String status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, Object> body = Map.of("status", status);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void sendError(HttpServletResponse response, String error) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        Map<String, Object> body = Map.of("error", error);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

