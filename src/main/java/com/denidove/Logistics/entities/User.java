package com.denidove.Logistics.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column (nullable = false)
    private String login;

    @Column (nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    private Role role;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Task> taskList;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    private boolean enabled;

    // Получаем первую букву имени пользователя
    public String getInitials() {
        return String.valueOf(name.charAt(0));
    }
}
