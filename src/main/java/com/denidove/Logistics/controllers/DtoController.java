package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DtoController {

    private final UserSessionService userSessionService;

    @PostMapping("/api/calc-dto")
    public void saveOrderDto(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        // Достаём guest_id из cookie
        String guestId = userSessionService.getGuestIdFromCookie(request);
        // Просто сохраняем состояние корзины неавторизованного пользователя в Redis
        userSessionService.saveGuestTask(guestId, taskDto);

        //List<City> cities = List.of(City.values());

        // В старой версии: сохраняли состояние корзины неавторизованного пользователя (в рамках сессии)
        //taskService.saveToDto("default", taskDto);

        //return "redirect:/";
        //return "login_1";
    }
}
