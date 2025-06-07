package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.Role;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.repositories.RoleRepository;
import com.denidove.Logistics.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void save(User user) {

        Role userRole = roleRepository.getReferenceById(1);
        user.setRole(userRole);
        userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);

    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }
}
