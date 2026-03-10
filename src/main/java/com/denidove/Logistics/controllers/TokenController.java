package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TokenDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    // Сохранение нового пароля и редирект на главную страницу
    @PostMapping("/save-pass")
    public String newPassFormHandler(@RequestBody TokenDto tokenDto, RedirectAttributes redirectAttributes) {

        var login = userSessionService.getLoginByToken(tokenDto.getToken());
        if(login == null) {
            return "redirect:/auth/login-1?error=expired";
        }

        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        user.setPassword(tokenDto.getPassword());
        userService.save(user);

        String msg = "Ваш пароль успешно обновлен.";

        // Удаляем токен в Redis:
        userSessionService.deleteResetToken(tokenDto.getToken());

        redirectAttributes.addFlashAttribute("infoMessage", msg);
        return "redirect:/reset-page";
    }

}
