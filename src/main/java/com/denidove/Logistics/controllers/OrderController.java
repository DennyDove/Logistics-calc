package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.ResponseDto;
import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.services.PostRequestService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final TaskService taskService;
    private final UserSessionService userSessionService;
    private final PostRequestService postRequestService;

    public OrderController(TaskService taskService, UserSessionService userSessionService,
                           PostRequestService postRequestService) {
        this.taskService = taskService;
        this.userSessionService = userSessionService;
        this.postRequestService = postRequestService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> saveOrder(@RequestBody TaskDto taskDto) {

        Task task = new Task();
        task.setCargoName(taskDto.getCargoName());
        task.setStartPoint(taskDto.getStartPoint().getName());
        task.setDestination(taskDto.getDestination().getName());
        task.setWeight(taskDto.getWeight());

        boolean authStatus = userSessionService.getAuthStatus();

        task.setUser(userSessionService.getSecurityUser().getUser());
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
