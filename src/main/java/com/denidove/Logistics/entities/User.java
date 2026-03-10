package com.denidove.Logistics.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // По умолчанию поле всегда @Column(nullable = true),
    // Аннотация ниже означает NOT NULL - убрал т.к. в БД могут создаваться пустые поля
    //@Column(nullable = false)
    private String name;

    //@Column(nullable = false)
    private Integer age;

    @Column (unique = true)  //(nullable = false)
    private String login;

    @Column (unique = true)  //(nullable = false)
    private String email;

    @Column (unique = true) //(nullable = false)
    private String phone;

    //@Column(nullable = false)
    private String password;

    @ManyToOne
    private Role role;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private List<Task> taskList;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    private boolean enabled;

    @Column
    private boolean twoauth; // в названии булева поля не следует использовать предлог "is" при использовании библиотеки Jackson, иначе не будет маппиться

    // Получаем первую букву имени пользователя
    @JsonIgnore
    public String getInitials() {
        return String.valueOf(name.charAt(0));
    }
}
