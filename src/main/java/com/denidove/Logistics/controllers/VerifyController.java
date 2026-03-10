/**
 Данный класс используется:
 1) для подтверждения корректного кода, отправленного на почту при регистрации
 2) для восстановления забытого пароля:

    a) Рендеринг страницы сброса пароля
    @GetMapping("/reset-page")

    b) Обработка формы ввода логина
    @PostMapping("/reset-mail")

    c) Проверка отправленного из письма по ссылке токена и рендеринг страницы для ввода нового пароля с токеном, вставленном в hidden-поле формы
    @GetMapping("/reset")

    d) Сохранение нового пароля и редирект на главную страницу - в контроллере TokenController
    @PostMapping("/save-pass")

*/

package com.denidove.Logistics.controllers;

import com.denidove.Logistics.LogisticsApplication;
import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.TokenDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.yaml.snakeyaml.scanner.ScannerImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
public class VerifyController {

    private final UserService userService;
    private final UserSessionService userSessionService;
    private final SimpleMailService simpleMailService;

    private static final Logger log = LoggerFactory.getLogger(LogisticsApplication.class);

    /* Конструктор можно убрать, т.к. он создается автоматически через аннотацию @RequiredArgsConstructor
    public VerifyController(TaskService taskService, UserService userService) {
        this.userService = userService;
        this.taskService = taskService;
    }*/

    @GetMapping("auth/verify")
    public String saveOrderDto(Model model, @RequestParam("code") String code) {
        if(userService.verify(code)) {
            System.out.println("Verification confirmed!");
        }
        return "success_verify";  //  Можно писать success_verify.html, но предпочтительнее без "html"
    }

    // Рендеринг страницы сброса пароля
    @GetMapping("/reset-page")
    public String resetPageRendering(Model model) {
        var userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "pass_reset";
    }

    /*
    // Вариант контроллера, который выдает сообщение об отправке, через @RequestParam
    // Но более предпочтительно использовать ${param.*} только для query параметров
    // А для нормальных сообщений — flash-attributes. Рабочий вариант выше.
    @GetMapping("/reset-page")
    public String resetPage(@RequestParam(value = "message", required = false) String message, Model model) {
        var userDto = new UserDto();
        model.addAttribute("user", userDto);

        if(message != null) {
            model.addAttribute("msg", "На ваш адресс электронной почты выслано письмо со ссылкой для восстановления пароля.");
        }

        return "pass_reset";
    }*/

    // Обработка формы ввода логина
    @PostMapping("/reset-mail")
    public String resetFormHandler(@ModelAttribute("user") UserDto userDto,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {

        //toDo сделавть проверку Optional на Null
        //toDo Посмотреть почему, если возникает ошидка post/get запроса, тогда идет редирект на localhost:8443/login-1
        //toDo но в ряде случаев просто редирект на localhost:8080/login-1

        Optional<User> userOptional= userService.findByLogin(userDto.getUsername());
        if(userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        var user = userOptional.get();

        // Генерация и сохранение кода подтверждения для сбороса пароля
        String token = UUID.randomUUID().toString();
        userSessionService.saveResetToken(token, user.getLogin());

        //toDo разобраться, оптимизировать getSiteURL(request)
        try {
            simpleMailService.sendResetEmail(userSessionService.getSiteURL(request), user, token);
        } catch (Exception e) {
            log.error("Ошибка отправки письма восстановления: ", e);
        }

        String msg = "На ваш адресс электронной почты выслано письмо со ссылкой для восстановления пароля.";

        redirectAttributes.addFlashAttribute("infoMessage", msg);
        return "redirect:/reset-page";
    }

    // Проверка отправленного из письма по ссылке токена и рендеринг страницы для ввода нового пароля
    // с токеном, вставленном в hidden-поле формы
    @GetMapping("/reset")
    public String resetPassword(Model model, @RequestParam(value = "token") String token,
                                RedirectAttributes redirectAttributes) {

        var login = userSessionService.getLoginByToken(token);
        if(login == null) {
            return "redirect:/login-1?error=expired";
        }

        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        // Сохраняем временного пользователя для использовании в /save-pass - это неправильно!
        //userRedisService.setPendingUser(user);

        /* Плохая практика отправлять пароли на почту - критический уровень уязвимости
        try {
            simpleMailService.sendNewPassword(user, newPassword);
        } catch (Exception e) {
            log.error("Ошибка отправки письма восстановления: ", e);
        }

        String msg = "Текщий пароль успешно сброшен. Временный пароль отправлен на Ваш e-mail.";
        redirectAttributes.addFlashAttribute("successMessage", msg);
        return "redirect:/reset-page";
        */

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken(token);

        // Заносим значение временного токена в hidden-поле html формы:
        model.addAttribute("tokenDto", tokenDto);

        return "new_pass";

    }

}