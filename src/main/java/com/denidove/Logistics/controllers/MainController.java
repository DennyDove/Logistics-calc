package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final UserSessionService userSessionService;
    private final TaskService taskService;

    public MainController(UserSessionService userSessionService, TaskService taskService) {
        this.userSessionService = userSessionService;
        this.taskService = taskService;
    }

    @PostMapping("/order")
    public String saveOrder(Model model, @ModelAttribute("task") Task task) {

        boolean authStatus = userSessionService.getAuthStatus();

        if (authStatus) {

            task.setUser(userSessionService.getUser());
            Long orderId = taskService.save(task);
            model.addAttribute("orderId", orderId);
            return "orderok.html";
        }
        // Сценарий для неавторизовавшихся пользователей
        else {
            // Просто сохраняем состояние корзины неавторизованного пользователя (по quantity)
            //taskService.saveToDto();
            return "login.html";
        }
    }
}