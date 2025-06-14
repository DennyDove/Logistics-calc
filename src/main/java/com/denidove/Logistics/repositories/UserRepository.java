package com.denidove.Logistics.repositories;

import com.denidove.Logistics.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findUserByLogin(String login);

    public Optional<User> findByVerificationCode(String code);
}
