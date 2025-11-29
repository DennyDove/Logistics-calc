package com.denidove.Logistics.services;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@Service // Сервисный класс для операций с пользователем в рамках сессии.
@RequiredArgsConstructor
//@SessionScope - отключили // То есть состояние(значения полей) будет сохраняться в течение сессии (@SessionScope)

// Используется частично. Так как перешли от сессионного подхода на Redis-подход
public class UserSessionService {

    /* Для архива - ниже то, что осталось от старого сессионного подхода (//@SessionScope):
    private HashMap<String, TaskDto> taskDto = new HashMap<>();
    private String siteUrl; // переменная для записи части адрессной строки пользователя (для формирования ссылки подтверждения регистрации)
    // Поле ниже отключили, так как перешли на Redis
    private User pendingUser; // переменная для хранения промежуточного пользователя для 2-х факторной аутентификации
    */

    private final RedisTemplate<String, Object> redisTemplate;

    // Префикс ключей Redis для гостей и зарегистрированных пользователей
    private static final String REDIS_USER_PREFIX = "user:";

    // Префикс ключей Redis для восстановления пароля
    private static final String REDIS_RESET_PREFIX = "reset:token:";

    // Срок хранения данных гостя
    private static final Duration TTL = Duration.ofDays(3);
    private static final Duration TTL1 = Duration.ofMinutes(10);

    // ==============================
    // 🔹 Методы для SecurityContext
    // ==============================

    // Получаем статус авторизации пользователя (вариант для STATELESS, т.е. бессессионного подхода)
    public boolean getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Boolean check1 = authentication != null;
        Boolean check2 = authentication.isAuthenticated();
        Boolean check3 = (authentication instanceof AnonymousAuthenticationToken);

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }


    // Получаем авторизованного пользователя из контекста безопасности
    public SecurityUser getSecurityUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return (SecurityUser) auth.getPrincipal();
    }

    // =================================
    // 🔹 Методы для гостевого состояния
    // =================================

    /** Сохраняем временные данные (например, TaskDto) в Redis */
    public void saveGuestTask(String guestId, TaskDto taskDto) {
        if (guestId == null) return;
        String redisKey = REDIS_USER_PREFIX + guestId;
        redisTemplate.opsForHash().put(redisKey, "taskDto", taskDto);
        redisTemplate.expire(redisKey, TTL);
    }

    /** Получаем сохранённые данные гостя */
    public TaskDto loadGuestTask(String guestId) {
        if (guestId == null) return null;
        String redisKey = REDIS_USER_PREFIX + guestId;
        Object obj = redisTemplate.opsForHash().get(redisKey, "taskDto");
        return obj instanceof TaskDto ? (TaskDto) obj : null;
    }

    /** Очищаем данные гостя после регистрации/авторизации */
    public void clearGuestData(String guestId) {
        if (guestId == null) return;
        String redisKey = REDIS_USER_PREFIX + guestId;
        redisTemplate.delete(redisKey);
    }

    // Методы аналогичные save/loadGuestTask, но с добавление дополнительного элемента "company" в составной ключ Redis
    public void saveTaskDto(String login, String company, TaskDto taskDto) {
        if (login == null) return;
        String redisKey = REDIS_USER_PREFIX + login + ":" + company;
        redisTemplate.opsForHash().put(redisKey, "taskDto", taskDto);
        redisTemplate.expire(redisKey, TTL);
    }

    public TaskDto getTaskDto(String login, String key) {
        if (login == null) return null;
        String redisKey = REDIS_USER_PREFIX + login + ":" + key;
        Object obj = redisTemplate.opsForHash().get(redisKey, "taskDto");
        return obj instanceof TaskDto ? (TaskDto) obj : null;
    }

    /** Сохраняем токен e-mail подтверждения в Redis */
    public void saveResetToken(String token, String login) {
        if (login == null) return;
        String redisKey = REDIS_RESET_PREFIX + token;
        redisTemplate.opsForHash().put(redisKey, "login", login);
        redisTemplate.expire(redisKey, TTL1);
    }

    /**  Удаляем токен в Redis */
    public void deleteResetToken(String token) {
        if (token == null) return;
        String redisKey = REDIS_RESET_PREFIX + token;
        redisTemplate.delete(redisKey);
    }

    /** Получаем код e-mail подтверждения */
    public String getLoginByToken(String token) {
        if (token == null) return null;
        String redisKey = REDIS_RESET_PREFIX + token;
        Object obj = redisTemplate.opsForHash().get(redisKey, "login");
        return obj instanceof String ? (String) obj : null;
    }

    //toDo разобраться с 2-я методами ниже:

    public void saveSiteUrl(String login, String siteUrl) {
        if (login == null) return;
        String redisKey = REDIS_USER_PREFIX + login;
        redisTemplate.opsForHash().put(redisKey, "siteUrl", siteUrl);
        redisTemplate.expire(redisKey, TTL);
    }

    /** Получаем сохранённые данные гостя */
    public String getSiteUrl(String login) {
        if (login == null) return null;
        String redisKey = REDIS_USER_PREFIX + login;
        Object value = redisTemplate.opsForHash().get(redisKey, "siteUrl");
        return value != null ? value.toString() : null;
    }

    public String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();    // "http://localhost:8080/adduser"
        String deleteStr = request.getServletPath().toString(); // "/adduser"
        return siteURL.replace(deleteStr, "");       // результат: "http://localhost:8080/"
    }

    public String getGuestIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("guest_id".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

}
