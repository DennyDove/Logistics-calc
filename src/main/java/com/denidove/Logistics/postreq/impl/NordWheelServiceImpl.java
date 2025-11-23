package com.denidove.Logistics.postreq.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.exceptions.BadRequestException;
import com.denidove.Logistics.json.NordWheel;
import com.denidove.Logistics.json.NordWheelCities;
import com.denidove.Logistics.postreq.NordWheelService;
import com.denidove.Logistics.services.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NordWheelServiceImpl implements NordWheelService {

    private static final Logger log = LoggerFactory.getLogger(NordWheelServiceImpl.class);
    private final RestTemplate rest;
    private final TaskService taskService;

    private Integer startPoint = 0;
    private Integer finishPoint = 0;

    //@Value("${name.service.url}")
    private String logisticServiceUrl;

    public NordWheelServiceImpl(RestTemplate rest, TaskService taskService) {
        this.rest = rest;
        this.taskService = taskService;
    }

    // Отправка GET-запроса на сервис NordWheel
    public TaskDto sendRequest(TaskDto taskDto) throws JsonProcessingException {
        startPoint = 0;
        finishPoint = 0;
        setStartEndPoints(taskDto);
        float width = taskDto.getWidth().floatValue();
        float length = taskDto.getLength().floatValue();
        float height = taskDto.getHeight().floatValue();
        float weight = taskDto.getWeight().floatValue();
        String volume = String.valueOf(width * length * height);

        logisticServiceUrl = "https://nordw.ru/tools/api/calc/calculate/";
        String url = logisticServiceUrl; //+ "?token=fht828sJlJqu6Q96PBNLWe3cvPGocbDEBR86HFEC"; // токен доступа к API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("from", startPoint)
                .queryParam("to", finishPoint)
                .queryParam("deliver", "1")
                .queryParam("volume", volume)
                .queryParam("weight", weight)
                .queryParam("package", "0")
                .queryParam("insurance", "0")
                .queryParam("fragile", "0");

        url = builder.toUriString();

        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<NordWheel> response =
                rest.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

        var deliveryDetails = response.getBody();
            taskDto.setPrice(0.0); // обнуление для корректности нового запроса
            taskDto.setDays(0); // обнуление для корректности нового запроса
        if(deliveryDetails.getData().getTotal() != null) {
            Double price = deliveryDetails.getData().getTotal();
            Integer days = deliveryDetails.getData().getDays();

            taskDto.setCompanyName("Nord Wheel");
            taskDto.setCompanyLogo("nordw.jpg"); // в jar-архиве расширение файла JPG - чувствительно к регистру! При написании jpg - будет выдавать ошибку
            taskDto.setPrice(price);
            taskDto.setDays(days);

            // Просто сохраняем состояние запроса пользователя
            //toDo
            //taskService.saveToDto("nordw", taskDto);
        }
        return taskDto;
    }

    // Получение списка обслуживаемых городов
    private NordWheelCities getCityList() throws JsonProcessingException {
        logisticServiceUrl = "https://nordw.ru/tools/api/calc/destinations/";
        String url = logisticServiceUrl;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<NordWheelCities> response =
                rest.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    // Определение кода выбранноо города отправления и доставки
    private void setStartEndPoints(TaskDto taskDto) throws JsonProcessingException {
        var nordWheelCities = getCityList();
        var cityList = nordWheelCities.getData();
        for(NordWheelCities.Data c : cityList) {
            if(c.getName().equals(taskDto.getStartPoint())) {
                startPoint = c.getId();
            }
            if(c.getName().equals(taskDto.getDestination())) {
                finishPoint = c.getId();
            }
        }
        if(startPoint == 0 || finishPoint == 0) {
            throw new BadRequestException("Указанный город отправления или доставки не обслуживается");
        }
    }

    // Получение абсолютного сетевого пути из текушей среды
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