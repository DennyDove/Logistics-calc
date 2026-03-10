package com.denidove.Logistics.services.impl;

import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.Role;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.exceptions.DuplicateUserException;
import com.denidove.Logistics.repositories.RoleRepository;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserService;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    //private final EmailService emailService;
    private final SimpleMailService simpleMailService;

    @Override
    public void save(User user) {

        Role userRole = roleRepository.getReferenceById(1);
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);

        userRepository.save(user);
    }

    @Override
    public void register(User user) {

        //toDo сделать UserDto
        if (!user.getLogin().isBlank() && userRepository.existsByLogin(user.getLogin())) {
            throw new DuplicateUserException("LOGIN_ALREADY_EXISTS");
        }
        if (!user.getEmail().isBlank() && userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("EMAIL_ALREADY_EXISTS");
        }
        if (!user.getPhone().isBlank() && userRepository.existsByPhone(user.getPhone())) {
            throw new DuplicateUserException("PHONE_ALREADY_EXISTS");
        }

        Role userRole = roleRepository.getReferenceById(1);
        user.setRole(userRole);
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);

        userRepository.save(user);

        try {
            simpleMailService.sendRegEmail(user, randomCode);
        } catch (Exception e) {}
        //emailService.sendRegistrationEmail(user, randomCode);
    }

    // Подтверждение регистрации пользователя
    public boolean verify(String code) {
        User user = userRepository.findByVerificationCode(code).get();
        if(user == null || user.isEnabled()) return false;
        else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);
            return true;
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);

    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

}

