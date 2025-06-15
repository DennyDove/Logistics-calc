package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final TaskService taskService;

    public MainController(UserSessionService userSessionService, TaskService taskService,
                          UserService userService) {
        this.userSessionService = userSessionService;
        this.userService = userService;
        this.taskService = taskService;
    }

    @PostMapping("/order-dto")
    public String saveOrderDto(Model model, @ModelAttribute("task") TaskDto taskDto) {

        //toDO сделать ещё один end-point "/order-auth" там данные будут отправляться через JavaScript
        // @ModelAttribute("task") в данном случае уже не работает т.к. из формы удалено th:object
        // Только th:object работает с @ModelAttribute("task")

        // Просто сохраняем состояние корзины неавторизованного пользователя
        taskService.saveToDto(taskDto);
        return "login.html";
    }

    @GetMapping("/verify")
    public String saveOrderDto(Model model, @RequestParam("code") String code) {
        if(userService.verify(code)) {
            System.out.println("Verification confirmed!");
        }
        //return "redirect:/";
        return "success_verify.html";
    }

}