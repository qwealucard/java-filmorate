package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Email(message = "Некорректный формат email")
    private String email;
    @NotNull
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;


    // Связь многие ко многим с друзьями (пользователь может быть другом другого пользователя)
    @ManyToMany(fetch = FetchType.EAGER) // Загружает друзей сразу
    @JoinTable(
            name = "friendships",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

}
