package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userFromDb = userService.findByLogin(username);
        if (userFromDb.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new SecurityUser(userFromDb.get());
    }
}
