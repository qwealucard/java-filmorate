package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

@Slf4j
@Repository
@AllArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(Integer filmId, Integer userId) {
        if (!isFilmExists(filmId)) {
            throw new IllegalArgumentException("Фильма с id " + filmId + " не существует.");
        }
        log.info("Добавляем лайк для фильма с id " + filmId + " от пользователя с id " + userId);
        if (isLikeAlreadyAdded(filmId, userId)) {
            return;
        }
        String sql = "INSERT INTO film_likes (user_id, film_id) VALUES (?, ?)";
        jdbc.update(sql, userId, filmId);
    }

    private boolean isFilmExists(Integer filmId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)";
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, filmId);
        return Boolean.TRUE.equals(exists);
    }

    private boolean isLikeAlreadyAdded(Integer filmId, Integer userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM film_likes WHERE film_id = ? AND user_id = ?)";
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, filmId, userId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        if (!isLikeAlreadyAdded(filmId, userId)) {
            return;
        }
        log.info("Удаляем лайк для фильма с id " + filmId + " от пользователя с id " + userId);
        String sql = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";
        jdbc.update(sql, userId, filmId);
    }
}