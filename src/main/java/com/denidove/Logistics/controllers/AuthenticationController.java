package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.security.CustomAuthenticationProvider;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
✅
Вариант контроллера c AuthenticationManager
Подключаем AuthenticationManager на шаге 1 вместо ручной проверки логина и пароля
Задача: повысить надёжность проверки логина/пароля, добавить надёжности и гибкости.
*/
@Controller
@RequiredArgsConstructor  // данная аннотация создает конструктор полей private final
public class AuthenticationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpleMailService simpleMailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;
    private final UserRedisService userRedisService;
    private final JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    int counter = 0;


    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        boolean loginStatus = userSessionService.getAuthStatus();

        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        TaskDto taskDto; // null;  // TaskDto taskDto = new TaskDto();
        model.addAttribute("cities", cities);

        // Очень важно добавить элемент "task" для работы с <form th:action="@{/order}" method="post" th:object="${task}">
        // Не получилось реализовать отображение сохраненного неавторизованным пользователем задания

        // Достаём guest_id из cookie
        String guestId = getGuestIdFromCookie(request);

        if (!loginStatus) {
            taskDto = userSessionService.loadGuestTask(guestId);
            if (taskDto == null) taskDto = new TaskDto();
            // Если не добавить строчу ниже, то Thymeleaf будет выдавать ошибку и редиректить.
            // Но в данном случае редирект будет на localhost:8080/login-1, но не на localhost:8443/login-1

            model.addAttribute("task", taskDto); // Из этого объекта вставляются значения в ранее заполенные пользователем поля формы (сохраненные значения)
            return "index_unauth";

        } else {
            SecurityUser user = userSessionService.getSecurityUser();
            taskDto = userSessionService.loadGuestTask(guestId);
            if (taskDto == null) taskDto = new TaskDto();

            model.addAttribute("userInit", user.getInitials());
            model.addAttribute("name", user.getUsername());
            model.addAttribute("coinsInCart", 0);
            model.addAttribute("task", taskDto); // Из этого объекта вставляются значения в ранее заполенные пользователем поля формы (сохраненные значения)
            //userSessionService.clearGuestData(guestId); // Для обнуления данных в форме - перенес это в OrderController

            return "index_auth";
        }
    }

    private String getGuestIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("guest_id".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
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

        return "login_1"; // Можно писать login_1.html, но предпочтительнее без "html"
    }

    // -------------------------------------------------------------
    // 3️⃣  GET /login-2 — форма для кода подтверждения
    // -------------------------------------------------------------
    @GetMapping("/login-2")
    public String showVerifyPage(HttpServletResponse response, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Если пользователь успешно прошел проверку лоина и пароля (в CustomAuthenticationProvider)
        // и у него двухфакторка выключена (т.е. isTwoauth() == false), тогда сразу редирект на главную:

        //toDo сделали дополнительную проверку!!!
        if(auth != null && auth.isAuthenticated()) {
            // ✅ Успех — генерируем токен и сохраняем в cookie
            SecurityUser securityUser = ((SecurityUser) auth.getPrincipal());

            if(!securityUser.isTwoauth()) {
                String jwt = jwtService.generateToken(securityUser.getLogin());

                ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(15 * 60)
                        .sameSite("Strict")
                        .build();

                response.addHeader("Set-Cookie", cookie.toString());
                return "redirect:/";
            }
        }

        // Если двухфакторка включена (т.е. isTwoauth() == true), тогда реализуется сценарий ниже:
        var userId = (Long) auth.getPrincipal(); // в Principal мы сохранили userId, поэтому достаем principal;
        User pendingUser = userRedisService.getPendingUser(userId);

        if (pendingUser == null) {
            return "redirect:/login-1?error=expired";
        }

        /* Данный блок не нужен.
        if(pendingUser.isTwoauth()) {
            model.addAttribute("is2FAuth", true);
            return "login_2.html";
        else {
            /*
            Эта проверка была полезна в первом базовом варианте двухфакторки,
            где контекст создавался вручную после подтверждения кода и не сохранялся в HttpSession автоматически.
            Но во втором базовом варианте (через AuthenticationManager) она уже не нужна —
            поскольку Spring Security сам восстанавливает контекст между запросами.
            var authauth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
                return "redirect:/login-1?error=bad_credentials";
            }
        }
        // Редирект при успешной аутентификации:
        return "redirect:/";
        */

        model.addAttribute("is2FAuth", true);
        return "login_2.html"; // Можно писать login_2.html, а можно и без "html"
    }


    // -------------------------------------------------------------
    // 4️⃣  POST /verify-code — проверка кода подтверждения
    // -------------------------------------------------------------
    @PostMapping("/verify-code")
    public String verifyCode(Model model, @RequestParam("code") String code,
                             HttpServletResponse response, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) auth.getPrincipal(); // в Principal мы сохранили userId, поэтому достаем principal;
        User pendingUser = userRedisService.getPendingUser(userId);
        if (pendingUser == null) {
            return "redirect:/login-1?error=expired";
        }

        if (!code.equals(pendingUser.getVerificationCode())) {
            // Каждый раз отправляется новый код
            // Генерация и отправка кода
            String randomCode = RandomString.make(6);
            pendingUser.setVerificationCode(randomCode);
            userRedisService.setPendingUser(pendingUser);

            try {
                simpleMailService.sendLoginEmail(pendingUser, randomCode);
            } catch (Exception e) {
                log.error("Ошибка при отправке e-mail уведомлений: {}", e.getMessage());
            }

            model.addAttribute("errorMessage", "Неверный код подтверждения.");
            model.addAttribute("is2FAuth", true);
            return "login_2.html";  // Можно писать login_2.html, но предпочтительнее без "html"
        }

        // ✅ Успех — генерируем токен и сохраняем в cookie
        String jwt = jwtService.generateToken(pendingUser.getLogin());

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());


        //toDo новые правки --- ВАЖНО: инвалидируем сессию и удаляем JSESSIONID (для большей надежности и предсказуемости)---

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ResponseCookie removeSession = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", removeSession.toString());

        // Очистим SecurityContext на сервере (чтобы не осталось pending auth в текущем потоке)
        SecurityContextHolder.clearContext();

        // Удаляем pending
        userRedisService.clearPendingAuth(pendingUser.getId());

        return "redirect:/";

        /*
        // ✅ Всё верно — создаём полную аутентификацию (создаём SecurityUser и токен)
        SecurityUser securityUser = new SecurityUser(pendingUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());

        // Раньше было:
        //SecurityContextHolder.getContext().setAuthentication(authToken);
        // В данном случае, контекст живёт только до конца текущего запроса — а после редиректа (resp.sendRedirect("/")) сессия «забывает», что пользователь уже вошёл.
        // Поэтому правильный вариант такой:
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken); //ставим auth в текущий контекст
        SecurityContextHolder.setContext(context);

        // явно сохраняем контекст в HttpSession, чтобы он пережил редирект
        HttpSession session = request.getSession(true); // обязательно true — создаём сессию, если её нет
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        userRedisService.clearPendingAuth(userId); // вручную очищаем временного пользователя, но можно и не очищать Redis сам удалит через 10 мин.

        return "redirect:/";
        */
    }

    //toDo Методы ниже вынести в отдельный контроллер

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
    public String userProfile(Model model, @RequestParam(value="id") Long id) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("orderId", id);
        return "order_ok.html";
    }
}