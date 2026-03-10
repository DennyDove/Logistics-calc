package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.exceptions_old.CalcRequestException;
import com.denidove.Logistics.exceptions_old.IncorrectDimensionException;
import com.denidove.Logistics.services.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class VozController {

    private final TaskService taskService;
    private final VozService vozService;

    public VozController(TaskService taskService, VozService vozService) {
        this.taskService = taskService;
        this.vozService = vozService;
    }

    @PostMapping("/api/vozcalc")
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