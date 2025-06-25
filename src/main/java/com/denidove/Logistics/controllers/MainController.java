package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.PostRequestService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final UserService userService;
    private final TaskService taskService;
    private final PostRequestService postRequestService;

    public MainController(TaskService taskService, UserService userService,
                          PostRequestService postRequestService) {
        this.userService = userService;
        this.taskService = taskService;
        this.postRequestService = postRequestService;
    }

    @PostMapping("/order-dto")
    public String saveOrderDto(Model model, @ModelAttribute("task") TaskDto taskDto, UserDto userDto) {

        //toDO сделать ещё один end-point "/order-auth" там данные будут отправляться через JavaScript
        // @ModelAttribute("task") в данном случае уже не работает т.к. из формы удалено th:object
        // Только th:object работает с @ModelAttribute("task")

        try {
            System.out.println(postRequestService.sendLogisticTestRequest());
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        }

        // Просто сохраняем состояние корзины неавторизованного пользователя
        taskService.saveToDto(taskDto);
        model.addAttribute("user", userDto);
        return "login_1.html";
    }

    @GetMapping("/verify")
    public String saveOrderDto1(Model model, @RequestParam("code") String code) {
        if(userService.verify(code)) {
            System.out.println("Verification confirmed!");
        }
        //return "redirect:/";
        return "success_verify.html";
    }

}