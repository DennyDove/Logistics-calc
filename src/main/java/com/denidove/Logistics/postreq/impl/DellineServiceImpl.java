package com.denidove.Logistics.postreq.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.exceptions.RestTemplateResponseErrorHandler;
import com.denidove.Logistics.json.DelLine;
import com.denidove.Logistics.postreq.DellineService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.denidove.Logistics.utils.CalendarUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;


@Component
public class DellineServiceImpl implements DellineService {

    private static final Logger log = LoggerFactory.getLogger(DellineServiceImpl.class);
    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final TaskService taskService;
    private final UserSessionService userSessionService;


    //@Value("${name.service.url}")
    private String logisticServiceUrl;

    public DellineServiceImpl(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper, TaskService taskService,
                              UserSessionService userSessionService) {
        this.rest = restTemplateBuilder
                .errorHandler(new RestTemplateResponseErrorHandler()) // добавили кастомный RestTemplateResponseErrorHandler
                .build();
        this.mapper = mapper;
        this.taskService = taskService;
        this.userSessionService = userSessionService;

    }

    // Отправка POST-запроса на сервис Delline
    @Override
    public TaskDto sendRequest(HttpServletRequest servletRequest, TaskDto taskDto) throws JsonProcessingException, HttpStatusCodeException {
        logisticServiceUrl = "https://api.dellin.ru/v2/calculator";
        String url = logisticServiceUrl; //+ "?token=G934UM29wXG2IKYS9iX9oKQCeojkApHSEWAxOC5v";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String appkey = "DADF6BBF-7EE8-40A0-9118-C169FBD949C4"; // токен доступа к API
        // Создаем объект Delline по заданным параметрам:
        var logistics = objectToJson(appkey, taskDto);
        // Трансформируем объект в json строку:
        mapper.writeValueAsString(logistics);

        HttpEntity<DelLine> request = new HttpEntity<>(logistics, headers);
        ResponseEntity<LinkedHashMap<String, Object>> response = rest.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
        var deliveryDetails = response.getBody();
            taskDto.setPrice(0.0); // обнуление для корректности нового запроса
            taskDto.setDays(0); // обнуление для корректности нового запроса
        if(deliveryDetails.get("data") != null) {
            var result = (LinkedHashMap<String, Double>) deliveryDetails.get("data"); //toDo прописать пояснения этой конструкции
            var resultDates = (LinkedHashMap<String, LinkedHashMap>) deliveryDetails.get("data"); //toDo прописать пояснения этой конструкции
            var orderDates = resultDates.get("orderDates");
            var startDate = orderDates.get("derivalFromOspSender").toString();
            var deliveryDate = orderDates.get("derivalFromOspReceiver").toString();
            var days = CalendarUtils.getDays(startDate, deliveryDate); // определяем срок доставки
            var price = result.get("price");
            taskDto.setCompanyName("Деловые линии");
            taskDto.setCompanyLogo("delline.jpg"); // в jar-архиве расширение файла JPG - чувствительно к регистру! При написании jpg - будет выдавать ошибку
            taskDto.setPrice(price);
            taskDto.setDays(days);

            // Просто сохраняем состояние запроса пользователя
            boolean loginStatus = userSessionService.getAuthStatus();
            if(!loginStatus) {
                String guestId = userSessionService.getGuestIdFromCookie(servletRequest);
                userSessionService.saveTaskDto(guestId, "delline", taskDto);
            } else {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var securityUser = (SecurityUser) auth.getPrincipal();
                var login = securityUser.getLogin();
                userSessionService.saveTaskDto(login, "delline",taskDto);
            }
        }
        return taskDto;
    }

    // Метод создает объект Delline для последующей трансформации этого объекта в json для post-запроса к сервису "Деловых Линий"
    private DelLine objectToJson(String appkey, TaskDto taskDto) {
        String startPoint = taskDto.getStartPoint();
        String finishPoint = taskDto.getDestination();
        Double width = taskDto.getWidth();
        Double length = taskDto.getLength();
        Double height = taskDto.getHeight();
        String volume = String.valueOf(width * length * height);
        String totalWeight = String.valueOf(taskDto.getWeight());
        String oversizedVolume = volume;
        String oversizedWeight = totalWeight;

        // сериализация: формирование json-строки на основе объекта Delline
        var deliveryType = new DelLine.Delivery.DeliveryType("auto");
        var address1 = new DelLine.Delivery.Address(startPoint);
        //var address2 = new DelLine.Delivery.Address("47.204150, 39.701188");
        var address2 = new DelLine.Delivery.Address(finishPoint);
        var time = new DelLine.Delivery.Time("9:30", "19:00");
        var derival = new DelLine.Delivery.Derival("address", CalendarUtils.getDerivalDate(), time, address1);
        //var requirements = new String[]{"0x818e8ff1eda1abc349318a478659af08"};
        var requirements = new String[]{};
        var arrival = new DelLine.Delivery.Arrival("address", address2, time, requirements);
        //var packages = new DelLine.Delivery.Packages[]{new DelLine.Delivery.Packages("0xA6A7BD2BF950E67F4B2CF7CC3A97C111", 1)};
        //var delivery = new DelLine.Delivery(deliveryType, derival, arrival, packages);
        var delivery = new DelLine.Delivery(deliveryType, derival, arrival);
        var paymentCitySearch = new DelLine.Payment.PaymentCitySearch(finishPoint);
        var payment = new DelLine.Payment(paymentCitySearch, "cash");

        var cargo = new DelLine.Cargo(1, length.floatValue(), width.floatValue(), height.floatValue(),
                Float.parseFloat(volume), Float.parseFloat(totalWeight), Float.parseFloat(oversizedWeight), Float.parseFloat(oversizedVolume));
        return new DelLine(appkey, delivery, payment, cargo);
    }

    // Получение абсолютного сетевого пути из текушей среды
    @Override
    public String getBaseUrl(HttpServletRequest request) {
        // Get protocol, server name, port and context path
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // localhost
        int serverPort = request.getServerPort(); // 8080
        String contextPath = request.getContextPath(); // /myapp

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);
        return baseUrl.toString();
    }

}