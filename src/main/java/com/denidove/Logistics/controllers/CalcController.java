package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.services.PostRequestService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;

@Controller
public class CalcController {

    private final UserService userService;
    private final TaskService taskService;
    private final PostRequestService postRequestService;

    public CalcController(TaskService taskService, UserService userService,
                          PostRequestService postRequestService) {
        this.userService = userService;
        this.taskService = taskService;
        this.postRequestService = postRequestService;
    }

    @PostMapping("/calc")
    public String saveOrderDto(Model model, @ModelAttribute("task-dto") TaskDto taskDto, UserDto userDto) {

        //toDO сделать ещё один end-point "/order-auth" там данные будут отправляться через JavaScript
        // @ModelAttribute("task") в данном случае уже не работает т.к. из формы удалено th:object
        // Только th:object работает с @ModelAttribute("task")
        var delivery = new LinkedHashMap<String, LinkedHashMap>();
        var delivery2 = new LinkedHashMap<String, LinkedHashMap>();

        String startPoint = taskDto.getStartPoint().getName();
        String destination = taskDto.getDestination().getName();
        Double width = taskDto.getWidth();
        Double length = taskDto.getLength();
        Double height = taskDto.getHeight();

        String volume = String.valueOf(width * length * height);
        String weight = String.valueOf(taskDto.getWeight());


        try {
            delivery = postRequestService.sendVozovozRequest(startPoint, destination, volume, weight);
            delivery2 = postRequestService.sendDelLineRequest(startPoint, destination, length, width, height, volume, weight);
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        }

        String price = delivery.get("response").get("basePrice").toString();
        String price2 = delivery2.get("data").get("price").toString();

        // Просто сохраняем состояние корзины неавторизованного пользователя
        taskService.saveToDto(taskDto);
        model.addAttribute("user", userDto);
        model.addAttribute("price", price);
        model.addAttribute("price2", price2);
        return "index_auth.html";
        //return "login_1.html";
    }

}