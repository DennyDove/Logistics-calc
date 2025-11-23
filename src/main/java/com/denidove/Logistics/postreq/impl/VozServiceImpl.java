package com.denidove.Logistics.postreq.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.json.Vozovoz;
import com.denidove.Logistics.postreq.VozService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Component
public class VozServiceImpl implements VozService {

    private static final Logger log = LoggerFactory.getLogger(VozServiceImpl.class);
    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final TaskService taskService;
    private final UserSessionService userSessionService;

    //@Value("${name.service.url}")
    private String logisticServiceUrl;

    public VozServiceImpl(RestTemplate rest, ObjectMapper mapper, TaskService taskService,
                          UserSessionService userSessionService) {
        this.rest = rest;
        this.mapper = mapper;
        this.taskService = taskService;
        this.userSessionService = userSessionService;
    }

    // Отправка POST-запроса на сервис Vozavoz
    @Override
    public TaskDto sendRequest(HttpServletRequest servletRequest, TaskDto taskDto) throws JsonProcessingException {
        logisticServiceUrl = "https://vozovoz.ru/api/";
        String url = logisticServiceUrl + "?token=fht828sJlJqu6Q96PBNLWe3cvPGocbDEBR86HFEC"; // токен доступа к API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создаем объект Vozovoz по заданным параметрам:
        var logistics = objectToJson("1", taskDto);
        // Трансформируем объект в json строку:
        mapper.writeValueAsString(logistics);

        HttpEntity<Vozovoz> request = new HttpEntity<>(logistics, headers);
        ResponseEntity<LinkedHashMap<String, LinkedHashMap>> response =
                rest.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
        var deliveryDetails = response.getBody();
            taskDto.setPrice(0.0); // обнуление для корректности нового запроса
            taskDto.setDays(0); // обнуление для корректности нового запроса

        String errorMsg = ""; // переменная для записи сообщения об ошибке, см. ниже в блоке try-catch

        if(deliveryDetails.get("response") != null) {
            Integer price = (Integer) deliveryDetails.get("response").get("basePrice");
            Integer days = ((LinkedHashMap<String, Integer>) deliveryDetails.get("response").get("deliveryTime")).get("from");
            taskDto.setCompanyName("Возавоз");
            taskDto.setCompanyLogo("vozovoz.jpg"); // в jar-архиве расширение файла JPG - чувствительно к регистру! При написании jpg - будет выдавать ошибку
            taskDto.setDays(days);
            taskDto.setPrice(price.doubleValue());

            // Просто сохраняем состояние запроса пользователя
            boolean loginStatus = userSessionService.getAuthStatus();
            if(!loginStatus) {
                String guestId = userSessionService.getGuestIdFromCookie(servletRequest);
                userSessionService.saveTaskDto(guestId, "vozovoz", taskDto);
            } else {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var securityUser = (SecurityUser) auth.getPrincipal();
                var login = securityUser.getLogin();
                userSessionService.saveTaskDto(login, "vozovoz", taskDto);
            }
        }

        if(deliveryDetails.get("error") != null) {
            errorMsg = deliveryDetails.get("error").get("message").toString();
            throw new CalcRequestException(errorMsg);
        }
        return taskDto;
    }

    // Метод создает объект Vozovoz для последующей трансформации этого объекта в json для post-запроса к сервису "Возовоз"
    private Vozovoz objectToJson(String quantity, TaskDto taskDto) {
        String startPoint = taskDto.getStartPoint();
        String finishPoint = taskDto.getDestination();
        float width = taskDto.getWidth().floatValue();
        float length = taskDto.getLength().floatValue();
        float height = taskDto.getHeight().floatValue();
        float weight = taskDto.getWeight().floatValue();
        String volume = String.valueOf(width * length * height);

        // сериализация: формирование json-строки на основе объекта Vozavoz
        var max = new Vozovoz.Params.Cargo.Dimension.Max(length, height, width, weight);
        var dimension = new Vozovoz.Params.Cargo.Dimension(quantity, volume, String.valueOf(weight), max);
        var cargo = new Vozovoz.Params.Cargo(dimension);
        var point1 = new Vozovoz.Params.Gateway.Point(startPoint, "default");
        var point2 = new Vozovoz.Params.Gateway.Point(finishPoint, "default");
        var dispatch = new Vozovoz.Params.Gateway.Dispatch(point1);
        var destination = new Vozovoz.Params.Gateway.Destination(point2);
        var gateway = new Vozovoz.Params.Gateway(dispatch, destination);
        var params = new Vozovoz.Params(cargo, gateway);
        return new Vozovoz("price", "get", params);
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