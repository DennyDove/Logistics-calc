package com.denidove.Logistics.postreq;

import com.denidove.Logistics.json.NordWheel;
import com.denidove.Logistics.json.NordWheelCities;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;

@Component
public class NordWheelService {

    private static final Logger log = LoggerFactory.getLogger(NordWheelService.class);
    private final RestTemplate rest;

    //@Value("${name.service.url}")
    private String logisticServiceUrl;

    public NordWheelService(RestTemplate rest) {
        this.rest = rest;
    }

    public NordWheelCities getCityList() throws JsonProcessingException {
        logisticServiceUrl = "https://nordw.ru/tools/api/calc/destinations/";
        String url = logisticServiceUrl;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<NordWheelCities> response =
                rest.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public NordWheel sendRequest(Integer startPoint, Integer finishPoint, Float weight, Float volume) throws JsonProcessingException {
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
        response.toString();
        return response.getBody();
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