package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Service
@SessionScope
// Сервисный класс для операций с пользователем в рамках сессии.
// То есть состояние(значения полей) будет сохраняться в течение сессии (@SessionScope)
public class UserSessionService {

    private final UserRepository userRepository;

    //private HashMap<Long, CartItemDto> cartItemDtoList = new HashMap<>();

    public UserSessionService(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    // Получаем статус авторизации пользователя
    public boolean getAuthStatus() {
        boolean authStatus = false;
        if (SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken)
            authStatus = true;
        return authStatus;
    }

    // Получаем авторизованного пользователя из контекста безопасности
    public User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
