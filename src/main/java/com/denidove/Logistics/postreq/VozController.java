package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class VozController {

    private final TaskService taskService;
    private final VozService vozService;

    public VozController(TaskService taskService, VozService vozService) {
        this.taskService = taskService;
        this.vozService = vozService;
    }

    @PostMapping("/vozcalc")
    public ResponseEntity<TaskDto> vozCalc(@RequestBody TaskDto taskDto) {

        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        var delivery = new LinkedHashMap<String, LinkedHashMap>();

        String startPoint = taskDto.getStartPoint();
        String destination = taskDto.getDestination();
        Double width = taskDto.getWidth();
        Double length = taskDto.getLength();
        Double height = taskDto.getHeight();

        String volume = String.valueOf(width * length * height);
        String weight = String.valueOf(taskDto.getWeight());

        if(length > 12.9 || width > 2.4 || height > 2.4) throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");
        String errorMsg = ""; // переменная для записи сообщения об ошибке, см. ниже в блоке try-catch

        try {
            delivery = vozService.sendRequest(startPoint, destination, length.floatValue(), width.floatValue(),
                    height.floatValue(), Float.valueOf(weight), volume);

            if(delivery.get("response") != null) {
                Integer price = (Integer) delivery.get("response").get("basePrice");
                Integer days = ((LinkedHashMap<String, Integer>) delivery.get("response").get("deliveryTime")).get("from");
                taskDto.setDays(days);
                taskDto.setPrice(price.doubleValue());

                // Просто сохраняем состояние запроса пользователя
                taskService.saveToDto("vozovoz", taskDto);
            }
            if(delivery.get("error") != null) {
                errorMsg = delivery.get("error").get("message").toString();
                throw new CalcRequestException(errorMsg);
            }
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException(errorMsg);
        }

        return ResponseEntity
                //.status(HttpStatus.FOUND)
                //.location(URI.create("http://localhost:8080/products"))
                .ok(taskDto);
    }
}