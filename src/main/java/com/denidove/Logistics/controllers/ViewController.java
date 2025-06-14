package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.swing.text.TabableView;
import java.util.List;

@Controller
public class ViewController {

    private final UserSessionService userSessionService;

    public ViewController(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @GetMapping("/hello")
    public String hello() {
        //toDo создвть обработку исключений
      return "hello.html";
    }

    @GetMapping("/")
    public String home(Model model) {
        boolean loginStatus = userSessionService.getAuthStatus();

        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);
        //Task task = new Task();
        TaskDto taskDto = new TaskDto();

        model.addAttribute("task", taskDto);
        model.addAttribute("cities", cities);


        // Очень важно добавить элемент "task" для работы с <form th:action="@{/order}" method="post" th:object="${task}">
        // Не получилось реализовать отображение сохраненного неавторизованным пользователем задания
        /*
        if(userSessionService.getTaskDto().isEmpty()) {
            model.addAttribute("task", taskDto);
            model.addAttribute("cities", cities);
        } else {*/
        if(!userSessionService.getTaskDto().isEmpty()) {
            taskDto = userSessionService.getTaskDto().getFirst();
            model.addAttribute("task", taskDto); // сделать Optional ?
            model.addAttribute("cities", cities);
        }

        if(loginStatus) {
            SecurityUser user = userSessionService.getSecurityUser();
            var userInit = user.getInitials();
            int coinsInCart = 0;
            //int coinsInCart = cartItemService.findAllByUserIdAndStatus().size();
            //model.addAttribute("task", task);
            model.addAttribute("userInit", userInit);
            model.addAttribute("coinsInCart", coinsInCart);
            model.addAttribute("name", user.getUsername());
            return "index_auth.html";
        } else {
            return "index.html";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration.html";
    }

    @GetMapping("/user_profile")
    public String userProfile(Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getUsername());
        model.addAttribute("login", user.getLogin());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("age", user.getAge());
        return "user_profile.html";
    }

    @GetMapping("/user-lk")
    public String user_lk(Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        //toDo
        //boolean isAdmin = false;
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getUsername());
        if(user.getRole().getAuthority().equals("Admin")) {return "admin_lk.html";}
        return "user_lk.html";
    }

    @GetMapping("/order-ok")
    public String userProfile(Model model, @RequestParam(name="id") Long id) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("orderId", id);

        return "order_ok.html";
    }
}