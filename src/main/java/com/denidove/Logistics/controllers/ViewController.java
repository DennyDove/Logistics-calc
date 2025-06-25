package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CredentialsException;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.PostRequestService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.TabableView;
import java.util.List;
import java.util.Optional;

@Controller
public class ViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PostRequestService postRequestService;

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

    @GetMapping("/login-1")
    public String login_1(Model model, UserDto userDto) {
        //User user = new User();
        model.addAttribute("user", userDto);

        return "login_1.html";
    }

    @PostMapping("/login-2")
    public String login_2(Model model, @ModelAttribute("user") UserDto userDto, HttpServletRequest request) {

        Optional<User> userOpt = userRepository.findUserByLogin(userDto.getUsername());

        if(userOpt.isEmpty()) throw new CredentialsException("Введены некорректнык данные!");

        //String loginServiceUrl = request.getContextPath();

        //postRequestService.sendLoginRequest(request, userDto);

        Boolean is2FAuth = true;

        User user = userOpt.get();

        if(is2FAuth) {
            String randomCode = RandomString.make(7);
            user.setVerificationCode(randomCode);
            userRepository.save(user);
            emailService.sendEmail_2(user.getEmail(), randomCode);

            model.addAttribute("is2FAuth", is2FAuth);
            model.addAttribute("user", userDto);
            return "login.html";
        } else {
            model.addAttribute("is2FAuth", is2FAuth);
            return "login.html";
        }
        //return "redirect:/";
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