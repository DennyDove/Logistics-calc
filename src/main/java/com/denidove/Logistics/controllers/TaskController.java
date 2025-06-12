package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class OrderController {

    private final UserService userService;
    private final TaskService taskService;
    private final UserSessionService userSessionService;

    public OrderController(UserService userService, TaskService taskService, UserSessionService userSessionService) {
        this.userService = userService;
        this.taskService = taskService;
        this.userSessionService = userSessionService;
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        User user = userSessionService.getUser();
        List<Task> taskList = taskService.findAllByUserId(user.getId());

        model.addAttribute("userInit", user.getInitials());
        model.addAttribute("taskList", taskList);

        return "user_order.html";

    }
}
