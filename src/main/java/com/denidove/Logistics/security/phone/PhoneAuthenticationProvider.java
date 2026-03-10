package com.denidove.Logistics.security.phone;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PhoneAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final UserRedisService redisService; // Хранит SMS-коды

    @Override
    public Authentication authenticate(Authentication authentication) {
        String phone = authentication.getName();
        String code = (String) authentication.getCredentials();

        // 1) Проверяем SMS-код
        String stored = redisService.getPendingAuth(phone);
        if (!Objects.equals(stored, code)) {
            throw new BadCredentialsException("Неверный код");
        }

        // 2) Ищем пользователя
        User user = userRepository.findUserByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        SecurityUser securityUser = new SecurityUser(user);

        // 3) Возвращаем аутентифицированный token
        return new PhoneAuthenticationToken(securityUser);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PhoneAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

