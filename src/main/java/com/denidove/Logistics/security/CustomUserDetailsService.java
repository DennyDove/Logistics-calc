package com.denidove.Logistics.security;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    //@Autowired
    //private UserService userService;
    @Autowired
    private  UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userFromDb = userRepository.findUserByLogin(username);
        if (userFromDb.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new SecurityUser(userFromDb.get());
    }
}
