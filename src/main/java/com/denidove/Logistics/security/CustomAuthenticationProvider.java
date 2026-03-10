package com.denidove.Logistics.security;

import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private SimpleMailService simpleMailService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        Optional<User> userOptional = userRepository.findUserByLogin(username);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findUserByPhone(username);
        }

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        User user = userOptional.get();
        SecurityUser securityUser = new SecurityUser(user);


        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Неверный пароль");
        }

        // ✅ если у пользователя включена 2FA — сохраняем сессию и ждём подтверждения кода
        if (user.isTwoauth()) {

            // Генерация и отправка кода
            String randomCode = RandomString.make(6);
            user.setVerificationCode(randomCode);
            userRedisService.setPendingUser(user); // сохраняем временный код в Redis. Раньше сохраняли в базу данных

            try {
                simpleMailService.sendLoginEmail(user, randomCode);
            } catch (Exception e) {
                log.error("Ошибка при отправке e-mail уведомлений: {}", e.getMessage());
            }

            //toDo - done!
            // Можно хранить userId прямо в Authentication (прямо в principal)!
            // вместо того чтобы возвращать токен с "username" в principal, можно вернуть токен, где principal = userId

            //toDo - done!
            // Возвращаем неаутентифицированный токен — SecurityContext не формируется
            return new UsernamePasswordAuthenticationToken(user.getId(), null);
            // 1) В principal вместо username заносим userId; 2) в credentials (password) можно смело ставить в null, чтобы не хранить пароль в памяти

        }

        // ✅ если 2FA нет — сразу авторизуем пользователя
        //toDo - done!
        // ВАЖНО: principal = SecurityUser, иначе токен не будет признан аутентифицированным

        //userRedisService.setPendingUser(user); // если 2FA == false то pendingUser создавать не нужно

        return new UsernamePasswordAuthenticationToken(
                securityUser, // было username
                null, // было password --> credentials (password) можно смело ставить в null, чтобы не хранить пароль в памяти
                securityUser.getAuthorities()
        );

        // Было:
        //return new UsernamePasswordAuthenticationToken(username, password, securityUser.getAuthorities());
        // этот вариант не работал

        /*
        💡 Объяснение:

        - principal теперь = SecurityUser, а не просто строка.
        - credentials (password) можно смело ставить в null, чтобы не хранить пароль в памяти.
        Теперь Spring Security понимает: “Пользователь успешно аутентифицирован”.

        Он создаёт SecurityContext, кладёт в сессию, и после редиректа на /login-2 —
        SecurityContextHolder.getContext().getAuthentication() уже будет содержать данные пользователя.

         */
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}