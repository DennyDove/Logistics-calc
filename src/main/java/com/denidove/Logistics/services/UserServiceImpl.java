package com.denidove.Logistics.services;

import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.entities.Role;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.RoleRepository;
import com.denidove.Logistics.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

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
    public void register(User user, String siteUrl) {

        Role userRole = roleRepository.getReferenceById(1);
        user.setRole(userRole);
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);

        userRepository.save(user);
        emailService.sendEmail(user.getEmail(), siteUrl, randomCode);
    }

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

    public Optional<User> findByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

}

