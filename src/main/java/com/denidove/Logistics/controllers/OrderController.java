package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.ResponseDto;
import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    private final TaskService taskService;
    private final UserSessionService userSessionService;

    public RestController(TaskService taskService, UserSessionService userSessionService) {
        this.taskService = taskService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> saveOrder(@RequestBody TaskDto taskDto) {

        Task task = new Task();
        task.setCargoName(taskDto.getCargoName());
        task.setStartPoint(taskDto.getStartPoint().getName());
        task.setDestination(taskDto.getDestination().getName());
        task.setWeight(taskDto.getWeight());

        boolean authStatus = userSessionService.getAuthStatus();

        task.setUser(userSessionService.getUser());
        Long orderId = taskService.save(task);
        userSessionService.getTaskDto().clear();
        //model.addAttribute("orderId", orderId);
        //return "orderok.html";

        ResponseDto responseDto = new ResponseDto();
        responseDto.setId(orderId);

        return ResponseEntity.ok().body(responseDto);

    }


    /*
    public ResponseEntity<?> addUser(@RequestBody User user) {
        userService.save(user);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:8080/products"))
                .build();
    }
    */

}
