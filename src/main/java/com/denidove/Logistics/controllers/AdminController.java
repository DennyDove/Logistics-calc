package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.swing.text.TabableView;

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
        Task task = new Task();

        // Очень важно добавить элемент "task" для работы с <form th:action="@{/order}" method="post" th:object="${task}">
        model.addAttribute("task", task);

        if(loginStatus) {
            User user = userSessionService.getUser();
            var userInit = user.getInitials();
            int coinsInCart = 0;
            //int coinsInCart = cartItemService.findAllByUserIdAndStatus().size();
            model.addAttribute("userInit", userInit);
            model.addAttribute("coinsInCart", coinsInCart);

            model.addAttribute("name", user.getName());
            return "index_auth.html";
        } else return "index.html";
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
        User user = userSessionService.getUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getName());
        model.addAttribute("login", user.getLogin());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("age", user.getAge());
        return "user_profile.html";
    }

    @GetMapping("/user_lk")
    public String user_lk(Model model) {
        User user = userSessionService.getUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getName());
        return "user_lk.html";
    }

    @GetMapping("/admin_lk")
    public String admin_lk(Model model) {
        User user = userSessionService.getUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);
        model.addAttribute("name", user.getName());
        return "admin_lk.html";
    }
}