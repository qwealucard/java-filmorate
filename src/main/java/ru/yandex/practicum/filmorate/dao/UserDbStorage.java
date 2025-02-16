package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Qualifier("userDbStorage")
@Repository
@AllArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    protected final JdbcTemplate jdbc;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (name, email, login, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.info("Starting user creation process for user: {}", user);


        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));

            log.debug("Prepared statement parameters: name='{}', email='{}', login='{}', birthday='{}'",
                    user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            int generatedId = keyHolder.getKey().intValue();
            user.setId(generatedId);
            log.info("User created successfully with ID: {}", generatedId);
        } else {
            log.warn("Failed to retrieve generated ID for user: {}", user);
        }

        log.info("User creation process completed: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
        int rowsUpdated;

        rowsUpdated = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            ps.setLong(5, user.getId());
            return ps;
        });

        if (rowsUpdated > 0) {
            return user;
        } else {
            throw new NotFoundException("Ошибка в обновлении пользователя");
        }
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        String sql = "SELECT id, name, email, login, birthday FROM users WHERE id = ?";
        try {
            User user = jdbc.queryForObject(sql, new UserRowMapper(), id);
            return Optional.of(user);
        } catch (DataAccessException e) {
            log.error("Ошибка при поиске пользователя с id:" + id + ": " + e.getMessage());
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, name, email, login, birthday FROM users ORDER BY id ASC";
        try {
            return jdbc.query(sql, (rs, rowNum) -> {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birthday").toLocalDate(),
                        new HashSet<>()
                );
                return user;
            });
        } catch (DataAccessException e) {
            log.error("Ошибка при получении всех пользователей" + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteUserById(Integer id) {
        try {

            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            int rowsAffected = jdbc.update(deleteUserSql, id);

            String deleteLikesSql = "DELETE FROM film_likes WHERE user_id = ?";
            jdbc.update(deleteLikesSql, id);

            String deleteFriendshipsSql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
            jdbc.update(deleteFriendshipsSql, id, id);

            if (rowsAffected == 0) {
                throw new NotFoundException("Пользователь с id " + id + " не найден");
            }
        }  catch (DataAccessException e) {
            throw new RuntimeException("Ошибка при удалении пользователя " + id + ": " + e.getMessage(), e);
        }
    }
}
