package com.denidove.Logistics.security;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    @Autowired
    private  UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userFromDb = userRepository.findUserByLogin(username);
        if (userFromDb.isEmpty()) {
            userFromDb = userRepository.findUserByPhone(username);
        }

        if (userFromDb.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new SecurityUser(userFromDb.get());
    }

    @Override
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by ID: " + id));

        return new SecurityUser(user);
    }
}
