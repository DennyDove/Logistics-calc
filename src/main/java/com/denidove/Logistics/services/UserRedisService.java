// Класс используется в механизме 2-х факторной аутентификации
package com.denidove.Logistics.services;

import com.denidove.Logistics.LogisticsApplication;
import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;

//@Getter
//@Setter
@Service
@RequiredArgsConstructor
public class UserRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate redisTemplateStr;
    private static final Logger log = LoggerFactory.getLogger(UserRedisService.class);

    //toDo удалить?
    private HashMap<String, TaskDto> taskDto = new HashMap<>();

    private String siteUrl; // переменная для записи части адрессной строки пользователя (для формирования ссылки подтверждения регистрации)

    private static final String KEY_pendingUser = "pendingUser:"; // Префикс для параметра "pendingUser"
    //private static final String KEY_taskDto = "taskDto:"; // Префикс для параметра "taskDto"

    private static final String KEY_pendingSms = "sms:pending:"; // Префикс для параметра Redis

    private static final Duration TTL = Duration.ofMinutes(10); // Время жизни записи в Redis


    //toDo проверить работу данных методов при отключении @SessionScope
    // Получаем статус авторизации пользователя
    public boolean getAuthStatus() {
        boolean authStatus = false;
        if (SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken)
            authStatus = true;
        return authStatus;
    }

    //toDo проверить работу данных методов при отключении @SessionScope
    // Получаем авторизованного пользователя из контекста безопасности
    public SecurityUser getSecurityUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ✅ Сеттеры, геттеры для объекта "PendingUser"
    // Создание записи ожидания подтверждения (pending auth)
    public void setPendingUser(User user) {
        String key = KEY_pendingUser + user.getId(); // 🔥 Раньше было user.getUsername()
        redisTemplate.opsForValue().set(key, user, TTL);
    }

    // Получить pending-пользователя
    public User getPendingUser(Long userId) {
        String key = KEY_pendingUser + userId;
        Object obj = redisTemplate.opsForValue().get(key);
        return (obj instanceof User) ? (User) obj : null;
    }

    // Очистить запись
    public void clearPendingAuth(Long userId) {
        String key = KEY_pendingUser + userId;
        redisTemplate.delete(key);
    }


//========================================================================================

    public void startPendingAuth(String phone, String code) {
        log.debug("CODE BEFORE SAVE = [{}]", code);

        String key = KEY_pendingSms + phone + ":code";
        redisTemplateStr.opsForValue().set(key, code, TTL);
    }

    // Получить pending-пользователя
    public String getPendingAuth(String phone) {
        String key = KEY_pendingSms + phone + ":code";

        log.debug("CODE FROM REDIS = [{}]", redisTemplateStr.opsForValue().get(key));
        String stored = redisTemplateStr.opsForValue().get(key); //toDo - в этом месте появлялись кавычки. Это было из-за использования разных RedisTemplate (Object и String)

        return redisTemplateStr.opsForValue().get(key);


    }

}
