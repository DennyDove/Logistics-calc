package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final UserSessionService userSessionService;
    private final TaskService taskService;
    //private final UserService userService;

    public AdminController(UserSessionService userSessionService, TaskService taskService) {
        this.userSessionService = userSessionService;
        this.taskService = taskService;
        //this.userService = userService;
    }

    @GetMapping("/admin-lk")
    public String admin_lk(Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getUsername());
        return "admin_lk.html";
    }

    @PostMapping("/admin-action")
    public String adminAction(@ModelAttribute("task") Task task) {
        var newStatus = task.getStatus();
        var userId = task.getUser().getId();
        var actualOrder = taskService.findByUserIdAndTaskId(userId, task.getId()).get();
        actualOrder.setStatus(newStatus);
        taskService.updateTaskAdmin(actualOrder);
        return "active_orders.html";
    }



}