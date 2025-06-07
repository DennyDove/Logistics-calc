package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.User;

import java.util.Optional;

public interface UserService {
    public void save(User user);
    public Optional<User> findById(Long id);
    public Optional<User> findByLogin(String login);

}
