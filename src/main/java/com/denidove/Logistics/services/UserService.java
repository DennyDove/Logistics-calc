package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface UserService {

    public void save(User user);
    public void register(User user, String siteUrl);
    public boolean verify(String code);
    public Optional<User> findById(Long id);
    public Optional<User> findByLogin(String login);
    public Optional<User> findByEmail(String email);

    //public boolean verify(String verificationCode);

}
