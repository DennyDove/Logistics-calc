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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> handleCalcRequestException(CalcRequestException ex) {
        return new ResponseEntity<>("Ошибка 400. Некорректный запрос. " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IncorrectDimensionException.class)
    public ResponseEntity<String> handleIncorrectDimensionException(IncorrectDimensionException ex) {
        return new ResponseEntity<>("Ошибка 400. Некорректный запрос. " + ex.getMessage(), HttpStatus.BAD_REQUEST);
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