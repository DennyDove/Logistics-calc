package com.denidove.Logistics.controllers;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
        userService.register(user, getSiteURL(request));
        //return "register_success";
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();    // "http://localhost:8080/adduser"
        String deleteStr = request.getServletPath().toString(); // "/adduser"
        return siteURL.replace(deleteStr, "");      // результат: "http://localhost:8080/"
    }


}
