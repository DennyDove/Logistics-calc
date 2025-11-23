package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.exceptions.BadRequestException;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.json.NordWheel;
import com.denidove.Logistics.json.NordWheelCities;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
public class NordWheelController {

    private  final UserSessionService userSessionService;
    private final TaskService taskService;
    private final NordWheelService nordWheelService;

    public NordWheelController(TaskService taskService, NordWheelService nordWheelService,
                               UserSessionService userSessionService) {
        this.taskService = taskService;
        this.nordWheelService = nordWheelService;
        this.userSessionService = userSessionService;

    }

    @PostMapping("/nordwcalc")
    public ResponseEntity<TaskDto> nordWheelCalc(@RequestBody TaskDto taskDto) {
        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        if(taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4)
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");

        try {
            var delivery = nordWheelService.sendRequest(taskDto);
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            throw new CalcRequestException("Некорректные параметры запроса. " + e.getMessage());
        }

        return ResponseEntity
                //.status(HttpStatus.FOUND)
                //.location(URI.create("http://localhost:8080/products"))
                .ok(taskDto);
    }
}