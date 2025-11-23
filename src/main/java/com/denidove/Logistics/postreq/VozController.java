package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.BadRequestException;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.json.DellineErr;
import com.denidove.Logistics.services.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.LinkedHashMap;

@RestController
public class VozController {

    private final TaskService taskService;
    private final VozService vozService;

    public VozController(TaskService taskService, VozService vozService) {
        this.taskService = taskService;
        this.vozService = vozService;
    }

    @PostMapping("/vozcalc")
    public ResponseEntity<TaskDto> vozCalc(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        if(taskDto.getLength() > 12.9 || taskDto.getWidth() > 2.4 || taskDto.getHeight() > 2.4)
            throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");

        try {
            var delivery = vozService.sendRequest(request, taskDto);
        } catch (CalcRequestException c) {
        throw new CalcRequestException(c.getMessage());

        } catch (Exception e) {
            throw new CalcRequestException(e.getMessage());
        }

        return ResponseEntity
                //.status(HttpStatus.FOUND)
                //.location(URI.create("http://localhost:8080/products"))
                .ok(taskDto);
    }
}