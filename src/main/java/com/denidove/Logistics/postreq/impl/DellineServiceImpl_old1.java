/*
package com.denidove.Logistics.postreq.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.exceptions.*;
import com.denidove.Logistics.json.DelLine;
import com.denidove.Logistics.json.DellineErr;
import com.denidove.Logistics.postreq.DellineService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.denidove.Logistics.utils.CalendarUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
public class DellineServiceImpl_old1 implements DellineService {

    private static final Logger log = LoggerFactory.getLogger(DellineServiceImpl_old1.class);

    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final TaskService taskService;
    private final UserSessionService userSessionService;

    // endpoint; можно вынести в @Value
    private final String logisticServiceUrl = "https://api.dellin.ru/v2/calculator";
    private final String appKey = "DADF6BBF-7EE8-40A0-9118-C169FBD949C4";

    public DellineServiceImpl_old1(RestTemplate rest, ObjectMapper mapper,
                                   TaskService taskService, UserSessionService userSessionService) {
        this.rest = rest;
        this.mapper = mapper;
        this.taskService = taskService;
        this.userSessionService = userSessionService;
    }

    @Override
    public TaskDto sendRequest(HttpServletRequest servletRequest, TaskDto taskDto) {
        validateDimensions(taskDto);

        DelLine body = buildRequestBody(taskDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DelLine> httpEntity = new HttpEntity<>(body, headers);

        try {
            ParameterizedTypeReference<Map<String, Object>> respType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<Map<String, Object>> response = rest.exchange(
                    logisticServiceUrl, HttpMethod.POST, httpEntity, respType);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || responseBody.get("data") == null) {
                throw new BadRequestException("Пустой ответ от Delline");
            }

            parseAndFillTaskDto(responseBody, taskDto);
            saveTaskDtoForUser(servletRequest, taskDto);

            return taskDto;

        } catch (HttpStatusCodeException ex) {
            // Delline вернул ошибку → парсим JSON-ответ
            String errBody = ex.getResponseBodyAsString();

            String detail = extractDellineErrorDetail(errBody);
            throw new DellineBadRequestException(detail);

        } catch (RestClientException rce) {
            log.error("Network error during Delline request", rce);
            throw new ExternalServiceException("Сетевая ошибка при запросе");

        } catch (AppException ae) {
            // перехватываем наши же исключения (BadRequestException)
            // и сразу пробрасываем — они уже корректные
            throw ae;

        }
    }

    private void validateDimensions(TaskDto taskDto) {
        if (taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4) {
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");
        }
    }

    private DelLine buildRequestBody(TaskDto taskDto) {
        String startPoint = taskDto.getStartPoint();
        String finishPoint = taskDto.getDestination();
        float width = taskDto.getWidth().floatValue();
        float length = taskDto.getLength().floatValue();
        float height = taskDto.getHeight().floatValue();
        float volume = width * length * height;
        float weight = taskDto.getWeight().floatValue();

        DelLine.Delivery.DeliveryType deliveryType = new DelLine.Delivery.DeliveryType("auto");
        DelLine.Delivery.Address addrFrom = new DelLine.Delivery.Address(startPoint);
        DelLine.Delivery.Address addrTo = new DelLine.Delivery.Address(finishPoint);
        DelLine.Delivery.Time time = new DelLine.Delivery.Time("9:30", "19:00");
        DelLine.Delivery.Derival derival = new DelLine.Delivery.Derival("address", CalendarUtils.getDerivalDate(), time, addrFrom);
        DelLine.Delivery.Arrival arrival = new DelLine.Delivery.Arrival("address", addrTo, time, new String[]{});
        DelLine.Delivery delivery = new DelLine.Delivery(deliveryType, derival, arrival);

        DelLine.Payment.PaymentCitySearch pcs = new DelLine.Payment.PaymentCitySearch(finishPoint);
        DelLine.Payment payment = new DelLine.Payment(pcs, "cash");

        DelLine.Cargo cargo = new DelLine.Cargo(1,
                length, width, height,
                volume, weight, weight, volume);

        return new DelLine(appKey, delivery, payment, cargo);
    }

    @SuppressWarnings("unchecked")
    private void parseAndFillTaskDto(Map<String, Object> responseBody, TaskDto taskDto) {
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

        // price may be Integer or Double -> handle Number
        Number priceNum = (Number) data.get("price");
        double price = priceNum != null ? priceNum.doubleValue() : 0.0;

        Map<String, Object> orderDates = (Map<String, Object>) data.get("orderDates");
        String startDate = orderDates != null ? String.valueOf(orderDates.get("derivalFromOspSender")) : null;
        String deliveryDate = orderDates != null ? String.valueOf(orderDates.get("derivalFromOspReceiver")) : null;
        int days = 0;
        if (startDate != null && deliveryDate != null) {
            days = CalendarUtils.getDays(startDate, deliveryDate);
        }

        taskDto.setCompanyName("Деловые линии");
        taskDto.setCompanyLogo("delline.jpg");
        taskDto.setPrice(price);
        taskDto.setDays(days);
    }

    private void saveTaskDtoForUser(HttpServletRequest servletRequest, TaskDto taskDto) {
        boolean loginStatus = userSessionService.getAuthStatus();
        if (!loginStatus) {
            String guestId = userSessionService.getGuestIdFromCookie(servletRequest);
            userSessionService.saveTaskDto(guestId, "delline", taskDto);
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var securityUser = (SecurityUser) auth.getPrincipal();
            var login = securityUser.getLogin();
            userSessionService.saveTaskDto(login, "delline", taskDto);
        }
    }

    private String extractDellineErrorDetail(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

            DellineErr err = mapper.readValue(body, DellineErr.class);

            // 1) errors[0].detail()
            if (err.errors() != null && err.errors().length > 0 && err.errors()[0].detail() != null) {
                return err.errors()[0].detail();
            }

            // 2) metadata.detail
            if (err.metadata() != null && err.metadata().detail() != null) {
                return err.metadata().detail();
            }

        } catch (Exception e) {
            log.warn("Failed to parse Delline error body: {}", body, e);
        }

        // 3) fallback
        return "Ошибка Delline API";
    }

    //toDo вариант метода для обычного POJO с геттерами и сеттерами (заменили на record)
    /*
    private String extractDellineErrorDetail(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

            DellineErr err = mapper.readValue(body, DellineErr.class);

            if (err.getErrors() != null && err.getErrors().length > 0) {
                return err.getErrors()[0].getDetail();
            }

            // fallback — попробуем вытащить metadata.detail
            if (err.getMetadata() != null) {
                return err.getMetadata().getDetail();
            }

        } catch (Exception e) {
            log.warn("Failed to parse Delline error body: {}", body);
        }
        // совсем fallback
        return "Ошибка Delline API";
    }


    @Override
    public String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);
        return baseUrl.toString();
    }
}
*/
