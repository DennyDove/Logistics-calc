package com.denidove.Logistics.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// Данный класс представляет обертку над персистентным классом User (@Entity User)
public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    //private String verificationCode;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(user.getRole());
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled(); //return true;
    }

    // Ниже идут геттеры, которые нужны для связи классов User и SecurityUser

    public User getUser() {
        return this.user;
    }

    public Long getId() {
        return user.getId();
    }

    // Получаем первую букву имени пользователя
    public String getInitials() {
        return String.valueOf(user.getName().charAt(0));
    }

    public String getLogin() {
        return user.getLogin();
    }

    public Integer getAge() {
        return user.getAge();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Role getRole() {
        return user.getRole();
    }

}
