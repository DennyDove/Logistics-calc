package com.denidove.Logistics.advice;


import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.exceptions.CredentialsException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(CredentialsException.class)
    public String handleCredentialsException(CredentialsException ex, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("user", userDto);
        return "login_1.html";
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