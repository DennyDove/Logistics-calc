package com.denidove.Logistics.postreq;

import com.denidove.Logistics.json.Vozovoz;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Component
public class VozService {

    private static final Logger log = LoggerFactory.getLogger(VozService.class);
    private final RestTemplate rest;
    private final ObjectMapper mapper;

    //@Value("${name.service.url}")
    private String logisticServiceUrl;

    public VozService(RestTemplate rest, ObjectMapper mapper) {
        this.rest = rest;
        this.mapper = mapper;
    }

    public LinkedHashMap sendRequest(String startPoint, String finishPoint, Float length, Float width, Float height,
                                     Float weight, String volume) throws JsonProcessingException {
        logisticServiceUrl = "https://vozovoz.ru/api/";
        String url = logisticServiceUrl + "?token=fht828sJlJqu6Q96PBNLWe3cvPGocbDEBR86HFEC"; // токен доступа к API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создаем объект Vozovoz по заданным параметрам:
        var logistics = objectToJson("1", length, width, height, weight, volume, startPoint, finishPoint);
        // Трансформируем объект в json строку:
        mapper.writeValueAsString(logistics);

        HttpEntity<Vozovoz> request = new HttpEntity<>(logistics, headers);
        ResponseEntity<LinkedHashMap<String, LinkedHashMap>> response =
                rest.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    // Метод создает объект Vozovoz для последующей трансформации этого объекта в json для post-запроса к сервису "Возовоз"
    private Vozovoz objectToJson(String quantity, Float length, Float width, Float height, Float weight, String volume, String start, String finish) {
        var max = new Vozovoz.Params.Cargo.Dimension.Max(length, width, height, weight);
        var dimension = new Vozovoz.Params.Cargo.Dimension(quantity, volume, String.valueOf(weight), max);
        var cargo = new Vozovoz.Params.Cargo(dimension);
        var point1 = new Vozovoz.Params.Gateway.Point(start, "default");
        var point2 = new Vozovoz.Params.Gateway.Point(finish, "default");
        var dispatch = new Vozovoz.Params.Gateway.Dispatch(point1);
        var destination = new Vozovoz.Params.Gateway.Destination(point2);
        var gateway = new Vozovoz.Params.Gateway(dispatch, destination);
        var params = new Vozovoz.Params(cargo, gateway);
        return new Vozovoz("price", "get", params);
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