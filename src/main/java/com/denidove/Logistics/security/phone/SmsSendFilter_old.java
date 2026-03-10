package com.denidove.Logistics.security.phone;

import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


//@Component
@RequiredArgsConstructor
public class SmsSendFilter_old extends OncePerRequestFilter {

    //toDo
    //private final SmsService smsService;
    private final SimpleMailService mailService; //временно замению смс на e-mail
    private final UserRedisService redisService;
    private final UserSessionService userSessionService; // храним pending phone
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Этот фильтр обрабатывает только отправку SMS
        if (!request.getRequestURI().equals("/api/auth/phone-send-code")
                || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

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
            //smsService.sendSms(phone, code);
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


