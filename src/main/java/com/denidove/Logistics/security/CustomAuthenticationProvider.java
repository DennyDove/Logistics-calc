package com.denidove.Logistics.security;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.exceptions.VerificationCodeErrorException;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final CustomUserDetailsService userDetailsService; // просто внедряем бин

    // Вариант установки PasswordEncoder после создания бина. Тоже самое можно сделать в конструкторе. См. ниже.
    /*
    @PostConstruct
    public void init() {
        this.setPasswordEncoder(new BCryptPasswordEncoder());
    }
    */

    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService) { //PasswordEncoder passwordEncoder) {
        super(userDetailsService); // здесь устанавливаем для AuthenticationProvider кастомный userDetailsService
        this.userDetailsService = userDetailsService;
        this.setPasswordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    public Authentication authenticate(Authentication auth)
            throws AuthenticationException {
        String verificationCode
                = ((CustomWebAuthenticationDetails) auth.getDetails())
                .getVerificationCode();

        /*
        SecurityUser securityUser = (SecurityUser) userDetailsService.loadUserByUsername(auth.getName()); //new SecurityUser(userService.findByLogin(auth.getName()).get());  // findByLogin
        if ((securityUser == null)) {
            throw new BadCredentialsException("Invalid username or password");
        }
        */

        /*
        if (user.isUsing2FA()) {
            Totp totp = new Totp(user.getSecret());
            if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid verfication code");
            }
        }
        */

        Boolean is2FAuth = false;

        String username = auth.getName();
        String password = auth.getCredentials().toString();

        UserDetails u = userDetailsService.loadUserByUsername(username);

        String codeFromDb = ((SecurityUser) u).getUser().getVerificationCode();


        if(is2FAuth) {
                if (!verificationCode.equals(codeFromDb)) {
                    throw new VerificationCodeErrorException("Введен неверный код подтверждения!");
                }
            }

        if (passwordEncoder.matches(password, u.getPassword())) {

            Authentication result = super.authenticate(auth); // усиливаем степень проверки
            return new UsernamePasswordAuthenticationToken(u, password, u.getAuthorities());
        } else {
            throw new BadCredentialsException("Something went wrong!");
        }

        /*
        Authentication result = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(
                securityUser, result.getCredentials(), result.getAuthorities());
        */

    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}