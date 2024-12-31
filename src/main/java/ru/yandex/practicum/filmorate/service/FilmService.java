package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;
    private final FriendshipStorage friendshipStorage;
    private final LikeStorage likeStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, MPAStorage mpaStorage, GenreStorage genreStorage,
                       FriendshipStorage friendshipStorage, LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;
    }

    public void addLike(Integer userId, Integer filmId) {
        likeStorage.addLike(userId, filmId);
    }

    public void removeLike(Integer userId, Integer filmId) {
        likeStorage.removeLike(userId, filmId);
    }

    public Genre addGenre(Genre genre) {
        return genreStorage.addGenre(genre);
    }

    public Optional<Genre> updateGenre(Genre genre) {
        Optional<Genre> update = genreStorage.update(genre);
        if (update == null) {
            throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
        }
        return update;
    }

    public Genre getGenreById(Integer id) {
        return genreStorage.getGenreById(id);
    }

    public List<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public boolean deleteGenreById(Integer id) {
        return genreStorage.deleteGenre(id);
    }

    public MPARating addMPARating(MPARating mpaRating) {
        return mpaStorage.addRating(mpaRating);
    }

    public MPARating getMPARatingById(Integer id) {
        MPARating mpaRating = mpaStorage.findRatingById(id);
        if (mpaRating == null) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return mpaRating;
    }

    public List<MPARating> getAllMPARatings() {
        return mpaStorage.findAll();
    }

    public void deleteMPARatingById(Integer id) {
        Integer rows = mpaStorage.deleteRating(id);
        if (rows == 0) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
    }

    public Optional<Film> getFilmById(Integer id) {
        return filmStorage.getFilmById(id);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }
}
