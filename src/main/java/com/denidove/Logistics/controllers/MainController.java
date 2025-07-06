package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MainController {

    private final UserService userService;
    private final TaskService taskService;

    public MainController(TaskService taskService, UserService userService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @PostMapping("/order-dto")
    public String saveOrderDto(Model model, @ModelAttribute("task") TaskDto taskDto, UserDto userDto) {

        //toDO сделать ещё один end-point "/order-auth" там данные будут отправляться через JavaScript
        // @ModelAttribute("task") в данном случае уже не работает т.к. из формы удалено th:object
        // Только th:object работает с @ModelAttribute("task")

        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);


        // Просто сохраняем состояние корзины неавторизованного пользователя
        taskService.saveToDto(taskDto);

        model.addAttribute("user", userDto);
        model.addAttribute("task", taskDto); // сделать Optional ?
        model.addAttribute("cities", cities);
        //return "index_auth.html";
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