/*
package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.exceptions_old.CalcRequestException;
import com.denidove.Logistics.exceptions_old.IncorrectDimensionException;
import com.denidove.Logistics.exceptions_old.BadRequestException;
import com.denidove.Logistics.json.DellineErr_old;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DellineController_Old {

    private final UserSessionService userSessionService;
    private final DellineService dellineService;

    public DellineController_Old(DellineService dellineService, UserSessionService userSessionService) {
        this.dellineService = dellineService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/delline")
    public ResponseEntity<TaskDto> dellineCalc(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);
        if(taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4)
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");

        try {
            var delivery = dellineService.sendRequest(request, taskDto);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String err = "";
                String responseBody = e.getResponseBodyAsString();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
                    DellineErr_old dellineErr = mapper.readValue(responseBody, DellineErr_old.class);
                    DellineErr_old.Errors[] errors = dellineErr.getErrors();
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
*/