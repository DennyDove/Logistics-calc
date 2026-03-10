package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.enums.TaskStatus;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final UserSessionService userSessionService;

    @GetMapping("/task-details")
    public String getTaskDetails(Model model, @RequestParam(value = "task", required = true) Long taskId) {
        SecurityUser user = userSessionService.getSecurityUser();
        Task task = taskService.findByUserIdAndTaskId(user.getId(), taskId).get();

        model.addAttribute("userInit", !user.getUsername().isEmpty() ? String.valueOf(user.getUsername().charAt(0)) : "A"); //user.getInitials());
        model.addAttribute("name", user.getUsername() != null ? String.valueOf(user.getUsername()) : "Anonymous"); //user.getUsername());
        model.addAttribute("task", task);

        return "task_details.html";
    }

    @GetMapping("/task-details-adm")
    public String getTaskDetailsAdm(Model model, @RequestParam(value = "user", required = true) Long userId,
                                    @RequestParam(value = "task", required = true) Long taskId) {

        List<TaskStatus> statusList = List.of(TaskStatus.InWork, TaskStatus.Done, TaskStatus.Archive);

        User user = userService.findById(userId).get();
        Task task = taskService.findByUserIdAndTaskId(userId, taskId).get();

        model.addAttribute("statusList", statusList);
        model.addAttribute("userInit", !user.getName().isEmpty() ? String.valueOf(user.getName().charAt(0)) : "A"); //user.getInitials());
        model.addAttribute("name", user.getName() != null ? String.valueOf(user.getName()) : "Anonymous"); //user.getUsername());
        model.addAttribute("task", task);

        return "task_details_adm.html";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        List<Task> taskList = taskService.findAllByUserId(user.getId());

        model.addAttribute("userInit", !user.getUsername().isEmpty() ? String.valueOf(user.getUsername().charAt(0)) : "A"); //user.getInitials());
        model.addAttribute("name", user.getUsername() != null ? String.valueOf(user.getUsername()) : "Anonymous"); //user.getUsername());
        model.addAttribute("taskList", taskList);

        return "user_orders.html";
    }

    @GetMapping("/active-orders")
    public String getActiveOrders(Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        List<Task> taskList = taskService.findAllByStatus(TaskStatus.InWork);

        model.addAttribute("userInit", !user.getUsername().isEmpty() ? String.valueOf(user.getUsername().charAt(0)) : "A"); //user.getInitials());
        model.addAttribute("taskList", taskList);

        return "active_orders.html";
    }
}
