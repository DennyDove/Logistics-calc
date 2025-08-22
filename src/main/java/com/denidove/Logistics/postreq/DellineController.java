package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.exceptions.BadRequestException;
import com.denidove.Logistics.json.DellineErr;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class DellineController {

    private  final UserSessionService userSessionService;
    private final TaskService taskService;
    private final DellineService dellineService;

    public DellineController(TaskService taskService,
                             DellineService dellineService, UserSessionService userSessionService) {
        this.taskService = taskService;
        this.dellineService = dellineService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/delline")
    public ResponseEntity<TaskDto> dellineCalc(@RequestBody TaskDto taskDto) {
        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);
        if(taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4)
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");

        try {
            var delivery = dellineService.sendRequest(taskDto);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                //toDo убрать лишнюю err
                String err = "";
                String responseBody = e.getResponseBodyAsString();
                /*
                Пока не полузилось реализовать сериализацию с помощью библиотеки Jackson, сделал такой топорный вариант
                извлечения сообщения об ошибке из строки
                int a = responseBody.lastIndexOf("detail") + 9;
                int b = responseBody.lastIndexOf("link") - 3;
                String err = responseBody.substring(a, b);*/

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
                                                                                //toDo
                    DellineErr dellineErr = mapper.readValue(responseBody, DellineErr.class);
                    DellineErr.Errors[] errors = dellineErr.getErrors();
                    err = errors[0].getDetail();
                } catch (IOException i) {
                    i.printStackTrace();
                }
                throw new BadRequestException(err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException("Некорректные параметры запроса");
        }
        return ResponseEntity
                //.status(HttpStatus.FOUND)
                //.location(URI.create("http://localhost:8080/products"))
                .ok(taskDto);
    }
}