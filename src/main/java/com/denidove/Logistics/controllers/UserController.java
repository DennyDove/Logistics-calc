package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
public class UserController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    public UserController(UserService userService, UserSessionService userSessionService) {
        this.userService = userService;
        this.userSessionService = userSessionService;
    }

    /*
    @PostMapping("/adduser")
    public void addUser(@RequestBody User user) {
        userService.save(user);
    }
    */

    /*
    public ResponseEntity<?> addUser(@RequestBody User user) {
        userService.save(user);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:8080/products"))
                .build();
    }
    */

    @PostMapping("/adduser")
    public void processRegister(@RequestBody User user, HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {
        //userService.save(user);

        userSessionService.setSiteUrl(getSiteURL(request));
        userService.register(user);
        //return "register_success";
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();    // "http://localhost:8080/adduser"
        String deleteStr = request.getServletPath().toString(); // "/adduser"
        return siteURL.replace(deleteStr, "");       // результат: "http://localhost:8080/"
    }


}
