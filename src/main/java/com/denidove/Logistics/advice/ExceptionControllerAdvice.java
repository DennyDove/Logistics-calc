package com.denidove.Logistics.advice;


import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.CredentialsException;

import com.denidove.Logistics.exceptions.DellineRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class ExceptionControllerAdvice {

    private final UserSessionService userSessionService;

    public ExceptionControllerAdvice(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @ExceptionHandler(CredentialsException.class)
    public String handleCredentialsException(CredentialsException ex, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("user", userDto);
        return "login_1.html";
    }

    @ExceptionHandler(CalcRequestException.class)
    public String handleCalcRequestException(CalcRequestException ex, Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        int coinsInCart = 0;
        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);

        TaskDto taskDto = userSessionService.getTaskDto().getLast();
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("price", userSessionService.getLogisticPrice().get(0));
        model.addAttribute("price2", userSessionService.getLogisticPrice().get(1));
        model.addAttribute("task", taskDto);
        model.addAttribute("cities", cities);
        model.addAttribute("userinit", userInit);
        model.addAttribute("coinsInChart", coinsInCart);
        return "index_auth.html";
    }

    @ExceptionHandler(DellineRequestException.class)
    public String handleDellineRequestException(DellineRequestException ex, Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        int coinsInCart = 0;
        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);

        TaskDto taskDto = userSessionService.getTaskDto().getLast();
        model.addAttribute("errorDelline", ex.getMessage());
        model.addAttribute("price", userSessionService.getLogisticPrice().get(0));
        model.addAttribute("price2", userSessionService.getLogisticPrice().get(1));
        model.addAttribute("task", taskDto);
        model.addAttribute("cities", cities);
        model.addAttribute("userinit", userInit);
        model.addAttribute("coinsInChart", coinsInCart);
        return "index_auth.html";
    }

    @ExceptionHandler(IncorrectDimensionException.class)
    public String handleIncorrectDimensionException(IncorrectDimensionException ex, Model model) {
        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        int coinsInCart = 0;
        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);

        TaskDto taskDto = userSessionService.getTaskDto().getLast();
        model.addAttribute("incorrectDimensionDelline", ex.getMessage());
        model.addAttribute("price", userSessionService.getLogisticPrice().get(0));
        model.addAttribute("price2", userSessionService.getLogisticPrice().get(1));
        model.addAttribute("task", taskDto);
        model.addAttribute("cities", cities);
        model.addAttribute("userinit", userInit);
        model.addAttribute("coinsInChart", coinsInCart);
        return "index_auth.html";
    }

    /* toDo данный обработчик исключений работает только с контроллерами, а здесь не контроллер, а AuthenticationProvider,
        поэтому обработчик исключений не работает
    @ExceptionHandler(VerificationCodeErrorException.class)
    public String handleVerificationCodeErrorException(VerificationCodeErrorException ex, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("verificationError", ex.getMessage());
        model.addAttribute("user", userDto);
        return "login_1.html";
    }
    */
}