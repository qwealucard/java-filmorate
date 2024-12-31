package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Slf4j
@Repository
@AllArgsConstructor
@Qualifier("FriendshipDbStorage")
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userMapper;

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String checkUserExists = "SELECT COUNT(*) FROM users WHERE id IN (?, ?)";
        String insertQuery = "INSERT INTO friendship(user_id, friend_id) VALUES(?,?)";
        try {
            Integer userCount = jdbc.queryForObject(checkUserExists, Integer.class, userId, friendId);
            if (userCount != 2) {
                throw new NotFoundException("Пользователь с id " + userId + " или id " + friendId + " не найден");
            }
            jdbc.update(insertQuery, userId, friendId);
            log.info("Друг добавлен");
        } catch (DataAccessException e) {
            throw new NotFoundException("Ошибка при добавлении друга: " + e.getMessage());
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        String deleteFriend = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        String checkUser = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer userCount = jdbc.queryForObject(checkUser, Integer.class, userId);
            if (userCount == null || userCount == 0) {
                throw new NotFoundException("Пользователь с id " + userId + " не найден");
            }
            Integer friendCount = jdbc.queryForObject(checkUser, Integer.class, friendId);
            if (friendCount == null || friendCount == 0) {
                throw new NotFoundException("Пользователь с id " + friendId + " не найден");
            }
            jdbc.update(deleteFriend, userId, friendId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Ошибка при удалении друга");
        }
    }

    @Override
    public List<User> getAllFriends(Integer id) {
        log.info("Получение всех друзей");
        String getFriends = "SELECT u.* " +
                "FROM users u " +
                "JOIN friendship f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        String checkUser = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer userCount = jdbc.queryForObject(checkUser, Integer.class, id);
            if (userCount == null || userCount == 0) {
                throw new NotFoundException("Пользователь с id " + id + " не найден");
            }
            return jdbc.query(getFriends, userMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Ошибка при получении списка друзей пользователя с id " + id + ": " + e.getMessage());
        }
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        String sql = "SELECT * FROM users " +
                "JOIN friendship friend1 ON users.id = friend1.friend_id " +
                "JOIN friendship friend2 ON users.id = friend2.friend_id " +
                "WHERE friend1.user_id = ? AND friend2.user_id = ?";
        return jdbc.query(sql, userMapper, userId, friendId);
    }
}