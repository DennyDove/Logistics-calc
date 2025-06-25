package com.denidove.Logistics.services;

import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.enums.RawJson;
import com.denidove.Logistics.json.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class PostRequestService {

    private final RestTemplate rest;
    private final ObjectMapper mapper;


    //@Value("${name.service.url}")
    private String loginServiceUrl;
    private String logisticServiceUrl;

    public PostRequestService(RestTemplate rest, ObjectMapper mapper) {
        this.rest = rest;
        this.mapper = mapper;
    }

    public String sendLogisticTestRequest() throws JsonProcessingException {

        logisticServiceUrl = "https://vozovoz.org/api/";

        String url = logisticServiceUrl + "?token=G934UM29wXG2IKYS9iX9oKQCeojkApHSEWAxOC5v";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //var rawJson = new RawJson();
        //String rawBody = rawJson.getRawJsonData();

        var logistics = new Logistics();

        Params params = new Params();
        Cargo cargo = new Cargo();
        Dimension dimension = new Dimension();
        dimension.setQuantity("1");
        dimension.setVolume("0.1");
        dimension.setWeight("0.9");

        cargo.setDimension(dimension);
        params.setCargo(cargo);

        Gateway gateway = new Gateway();
        Dispatch dispatch = new Dispatch();
        Destination destination = new Destination();
        Point point1 = new Point();
        point1.setLocation("Москва");
        point1.setTerminal("default");

        Point point2 = new Point();
        point2.setLocation("Саратов");
        point2.setTerminal("default");

        dispatch.setPoint(point1);
        destination.setPoint(point2);

        gateway.setDispatch(dispatch);
        gateway.setDestination(destination);

        params.setGateway(gateway);
        params.setDestination(destination);

        logistics.setObject("price");
        logistics.setAction("get");
        logistics.setParams(params);

        mapper.writeValueAsString(logistics);



        HttpEntity<Logistics> request = new HttpEntity<>(logistics, headers);

        ResponseEntity<String> response =
                rest.exchange(url, HttpMethod.POST, request, String.class);

        return response.getBody();
    }


    public void sendLoginRequest(HttpServletRequest request, UserDto userDto) {

        loginServiceUrl = getBaseUrl(request);

        String url = loginServiceUrl + "/login";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic");
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.add("requestId",
        //        UUID.randomUUID().toString());
        HttpEntity<UserDto> httpEntity = new HttpEntity<>(userDto, headers);


        var response = rest.exchange(url, HttpMethod.POST, httpEntity, UserDto.class);
        /*
        ResponseEntity<Payment> response =
                rest.exchange(uri,
                        HttpMethod.POST,
                        httpEntity,
                        Payment.class);
        return response.getBody();
        */
    }

    public String getBaseUrl(HttpServletRequest request) {
        // Get protocol, server name, port and context path
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // localhost
        int serverPort = request.getServerPort(); // 8080
        String contextPath = request.getContextPath(); // /myapp

        // Construct base URL
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);

        return baseUrl.toString();
    }
}
