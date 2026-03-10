package com.denidove.Logistics.postreq.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.exceptions.*;

import com.denidove.Logistics.json.DelLine;
import com.denidove.Logistics.json.DellineErr;
import com.denidove.Logistics.postreq.DellineService;
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

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Map;

/**
 * В данном сервисе выстроена новая архитектура отправки запроса на основе WebClient
 * В аналогичном классе VozServiceImpl применяется ещё старая архитектура на основе RestPemplate.
 * Два варианта оставлены для сравнения
*/


@Service
public class DellineServiceImpl implements DellineService {

    private final WebClient dellineWebClient;
    private final UserSessionService userSessionService;

    private final static Logger log = LoggerFactory.getLogger(DellineServiceImpl.class);

    // endpoint; можно вынести в @Value
    private final String logisticServiceUrl = "https://api.dellin.ru/v2/calculator";
    private final String appKey = "DADF6BBF-7EE8-40A0-9118-C169FBD949C4";

    public DellineServiceImpl(WebClient dellineWebClient, UserSessionService userSessionService) {
        this.dellineWebClient = dellineWebClient;
        this.userSessionService = userSessionService;
    }

    @Override
    public TaskDto sendRequest(HttpServletRequest request, TaskDto taskDto) {
        validateDimensions(taskDto);
        DelLine requestBody = buildRequestBody(taskDto);

        try {
            // === ВЫПОЛНЯЕМ ВНЕШНИЙ ЗАПРОС ===
            Map<String, Object> responseBody =
                    dellineWebClient.post()
                            .uri(logisticServiceUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, resp ->
                                    resp.bodyToMono(String.class)
                                            .map(this::extractDellineErrorDetail) // извлекаем/парсим ответ об ошибке
                                            .map(DellineBadRequestException::new)
                            )
                            .onStatus(HttpStatusCode::is5xxServerError, resp ->
                                    resp.bodyToMono(String.class)
                                            .map(body -> new ExternalServiceException("Сервер Delline недоступен"))
                            )
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block(); // это означает, что наше приложение не-реактивное

            if (responseBody == null || responseBody.get("data") == null) {
                throw new DellineBadRequestException("Пустой ответ от Delline");
            }

            // Парсим данные
            parseAndFillTaskDto(responseBody, taskDto);
            saveTaskDtoForUser(request, taskDto);

            return taskDto;

        } catch (AppException ae) {
            throw ae; // наши исключения — сразу наверх

        } catch (WebClientRequestException wce) {
            log.error("Network error calling Delline API", wce);
            throw new ExternalServiceException("Сетевая ошибка при запросе в Delline");

        }
    }

    //toDo попробовать оптимизировать
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

    private void validateDimensions(TaskDto taskDto) {
        if (taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4) {
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");
        }
    }

    private void parseAndFillTaskDto(Map<String, Object> responseBody, TaskDto taskDto) {

        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

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

    // === метод извлечения ошибки Delline ===
    private String extractDellineErrorDetail(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

            DellineErr err = mapper.readValue(body, DellineErr.class);

            if (err.errors() != null && err.errors().length > 0 && err.errors()[0].detail() != null) {
                return err.errors()[0].detail();
            }

            if (err.metadata() != null && err.metadata().detail() != null) {
                return err.metadata().detail();
            }

        } catch (Exception e) {
            log.warn("Failed to parse Delline error: {}", body);
        }
        return "Ошибка Delline API";
    }
}
