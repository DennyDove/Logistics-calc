package com.denidove.Logistics.services;

import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.enums.RawJson;
import com.denidove.Logistics.json.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;


@Component
public class PostRequestService {

    private static final Logger log = LoggerFactory.getLogger(PostRequestService.class);
    private final RestTemplate rest;
    private final ObjectMapper mapper;


    //@Value("${name.service.url}")
    private String loginServiceUrl;
    private String logisticServiceUrl;

    public PostRequestService(RestTemplate rest, ObjectMapper mapper) {
        this.rest = rest;
        this.mapper = mapper;
    }

    public LinkedHashMap sendVozovozRequest(String start, String finish, String volume, String weigth) throws JsonProcessingException {

        logisticServiceUrl = "https://vozovoz.org/api/";

        String url = logisticServiceUrl + "?token=G934UM29wXG2IKYS9iX9oKQCeojkApHSEWAxOC5v";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var logistics = vozovozObjectToJson("1", volume, weigth, start, finish);

        mapper.writeValueAsString(logistics);

        HttpEntity<Logistics> request = new HttpEntity<>(logistics, headers);

        ResponseEntity<LinkedHashMap<String, LinkedHashMap>> response =
                rest.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public LinkedHashMap sendDelLineRequest(String startPoint, String destination, Double length, Double width,
                                            Double height, String volume, String totalWeight, String oversizedWeight, String oversizedVolume) throws JsonProcessingException {

        logisticServiceUrl = "https://api.dellin.ru/v2/calculator";

        String url = logisticServiceUrl; //+ "?token=G934UM29wXG2IKYS9iX9oKQCeojkApHSEWAxOC5v";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var logistics = delLineObjectToJson("DADF6BBF-7EE8-40A0-9118-C169FBD949C4", startPoint, destination,
                length, width, height, volume, totalWeight, oversizedWeight, oversizedVolume);
        mapper.writeValueAsString(logistics);

        HttpEntity<DelLine> request = new HttpEntity<>(logistics, headers);

        ResponseEntity<LinkedHashMap<String, LinkedHashMap>> response =
                rest.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
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

    private Logistics vozovozObjectToJson(String quantity, String volume, String weigth, String start, String finish) {
        var dimension = new Logistics.Params.Cargo.Dimension(quantity, volume, weigth);
        var cargo = new Logistics.Params.Cargo(dimension);

        var point1 = new Logistics.Params.Gateway.Point(start, "default");
        var point2 = new Logistics.Params.Gateway.Point(finish, "default");
        var dispatch = new Logistics.Params.Gateway.Dispatch(point1);
        var destination = new Logistics.Params.Gateway.Destination(point2);
        var gateway = new Logistics.Params.Gateway(dispatch, destination);

        var params = new Logistics.Params(cargo, gateway);
        return new Logistics("price", "get", params);
    }

    private DelLine delLineObjectToJson(String appkey, String start, String finish, Double length, Double width, Double height,
                                        String volume, String totalWeight, String oversizedWeight, String oversizedVolume) {
        var deliveryType = new DelLine.Delivery.DeliveryType("auto");
        var address1 = new DelLine.Delivery.Address(start);
        //var address2 = new DelLine.Delivery.Address("47.204150, 39.701188");
        var address2 = new DelLine.Delivery.Address(finish);
        var time = new DelLine.Delivery.Time("9:30", "19:00");
        var derival = new DelLine.Delivery.Derival("address", "2025-07-05", time, address1);
        //var requirements = new String[]{"0x818e8ff1eda1abc349318a478659af08"};
        var requirements = new String[]{};
        var arrival = new DelLine.Delivery.Arrival("address", address2, time, requirements);
        var packages = new DelLine.Delivery.Packages[]{new DelLine.Delivery.Packages("0xA6A7BD2BF950E67F4B2CF7CC3A97C111", 1)};
        var delivery = new DelLine.Delivery(deliveryType, derival, arrival, packages);

        var paymentCitySearch = new DelLine.Payment.PaymentCitySearch("Москва");
        var payment = new DelLine.Payment(paymentCitySearch, "cash");

        var cargo = new DelLine.Cargo(1, length.floatValue(), width.floatValue(), height.floatValue(),
                Float.parseFloat(volume), Float.parseFloat(totalWeight), Float.parseFloat(oversizedWeight), Float.parseFloat(oversizedVolume));

        return new DelLine(appkey, delivery, payment, cargo);
    }
}