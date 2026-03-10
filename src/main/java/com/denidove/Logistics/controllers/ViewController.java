package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//@Controller - данный вариант контроллера отключен (в проекте просто для возможного доп. сценария)
public class ViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpleMailService simpleMailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserSessionService userSessionService;

    public ViewController(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    /*
    @GetMapping("/")
    public String home(Model model) {
        boolean loginStatus = userSessionService.getAuthStatus();

        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        TaskDto taskDto = new TaskDto();
        model.addAttribute("task", taskDto);
        model.addAttribute("cities", cities);

        // Очень важно добавить элемент "task" для работы с <form th:action="@{/order}" method="post" th:object="${task}">
        // Не получилось реализовать отображение сохраненного неавторизованным пользователем задания

        if(!userSessionService.getTaskDto().isEmpty()) {
            taskDto = userSessionService.getTaskDto().get("default");
            model.addAttribute("task", taskDto);
            model.addAttribute("cities", cities);
        }

        if(loginStatus) {
            SecurityUser user = userSessionService.getSecurityUser();
            var userInit = user.getInitials();
            int coinsInCart = 0;
            //int coinsInCart = cartItemService.findAllByUserIdAndStatus().size();
            model.addAttribute("userInit", userInit);
            model.addAttribute("coinsInCart", coinsInCart);
            model.addAttribute("name", user.getUsername());
            return "index_auth.html";
        } else {
            return "index.html";
        }
    }

    // -------------------------------------------------------------
    // 1️⃣  GET /login-1 — форма логина
    // -------------------------------------------------------------
    @GetMapping("/login-1")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        model.addAttribute("user", new UserDto());

        if (error != null) {
            switch (error) {
                case "bad_credentials" -> model.addAttribute("errorMessage", "Неверный логин или пароль.");
                case "invalid_code" -> model.addAttribute("errorMessage", "Неверный код подтверждения.");
                case "expired" -> model.addAttribute("errorMessage", "Сессия авторизации истекла. Повторите вход.");
                default -> model.addAttribute("errorMessage", "Ошибка входа. Попробуйте снова.");
            }
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Вы вышли из системы.");
        }

        return "login_1.html";
    }

    // -------------------------------------------------------------
    // 2️⃣  POST /login-1 — проверка логина и пароля --> Рабочий вариант без AuthenticationManager
    // -------------------------------------------------------------
    @PostMapping("/login-1")
    public String handleLogin(Model model, @ModelAttribute("user") UserDto userDto, HttpServletRequest request) {

        // ✅ Без AuthenticationManager самостоятельно проверяем пользователя и пароль:
        Optional<User> userOptional = userRepository.findUserByLogin(userDto.getUsername());
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Пользователь не найден");
            return "login_1.html";
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            model.addAttribute("errorMessage", "Неверный логин или пароль");
            return "login_1.html";
        }

        //✅ Если у пользователя включена 2FA:
        if (user.isTwoauth()) {
            // Генерация и отправка кода
            String randomCode = RandomString.make(6);
            user.setVerificationCode(randomCode);
            userRepository.save(user);

            try {
                simpleMailService.sendLoginEmail(user, randomCode);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Не удалось отправить код на почту");
                return "login_1.html";
            }

            // Сохраняем pending-пользователя в сессии
            userSessionService.setPendingUser(user);

            // Перенаправляем на страницу ввода кода
            return "redirect:/login-2";
        } else {
            //✅Если 2FA не требуется — логиним сразу
            SecurityUser securityUser = new SecurityUser(user);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());

            // ставим в текущий контекст
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            // явно сохраняем контекст в HttpSession, чтобы он пережил редирект
            HttpSession session = request.getSession(true); // обязательно true — создаём сессию, если её нет
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            return "redirect:/";
        }
    }

    // -------------------------------------------------------------
    // 3️⃣  GET /login-2 — форма для кода подтверждения
    // -------------------------------------------------------------
    @GetMapping("/login-2")
    public String showLogin2Form(Model model) {
        User pendingUser = userSessionService.getPendingUser();
        if (pendingUser == null) {
            return "redirect:/login-1?error=expired";
        }

        model.addAttribute("is2FAuth", true);
        return "login_2.html";
    }

    // -------------------------------------------------------------
    // 4️⃣  POST /verify-code — проверка кода подтверждения
    // -------------------------------------------------------------
    @PostMapping("/verify-code")
    public String verifyCode(Model model, @RequestParam("code") String code, HttpServletRequest request) {
        User pendingUser = userSessionService.getPendingUser();
        if (pendingUser == null) {
            return "redirect:/login-1?error=expired";
        }

        if (!code.equals(pendingUser.getVerificationCode())) {
            model.addAttribute("errorMessage", "Неверный код подтверждения.");
            model.addAttribute("is2FAuth", true);
            return "login_2.html";
        }

        // ✅ Всё верно — создаём полную аутентификацию (создаём SecurityUser и токен)
        SecurityUser securityUser = new SecurityUser(pendingUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());


        // Раньше было:
        //SecurityContextHolder.getContext().setAuthentication(auth);
        // В данном случае, контекст живёт только до конца текущего запроса — а после редиректа (resp.sendRedirect("/")) сессия «забывает», что пользователь уже вошёл.

        // Поэтому правильный вариант такой:
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken); //ставим auth в текущий контекст
        SecurityContextHolder.setContext(context);

        // явно сохраняем контекст в HttpSession, чтобы он пережил редирект
        HttpSession session = request.getSession(true); // обязательно true — создаём сессию, если её нет
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        userSessionService.clearPendingAuth();

        return "redirect:/";
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
        model.addAttribute("phone", user.getPhone());
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

    */
}