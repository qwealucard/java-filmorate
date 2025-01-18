DROP TABLE if EXISTS users CASCADE;
DROP TABLE if EXISTS films CASCADE;
DROP TABLE if EXISTS MPA_Ratings CASCADE;
DROP TABLE if EXISTS genres CASCADE;
DROP TABLE if EXISTS film_likes CASCADE;
DROP TABLE if EXISTS friendship CASCADE;
DROP TABLE if EXISTS film_genres CASCADE;
DROP TABLE if EXISTS recommendation CASCADE;
DROP TABLE if EXISTS film_directors CASCADE;
DROP TABLE if EXISTS directors CASCADE;
DROP TABLE if EXISTS recommendation CASCADE;
DROP TABLE if EXISTS user_feed CASCADE;

CREATE TABLE IF NOT EXISTS users (
   id SERIAL PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   login VARCHAR(50) NOT NULL UNIQUE,
   name VARCHAR(255),
   birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS MPA_Ratings (
   MPARating_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   MPA_Rating_name varchar(255)
);

CREATE TABLE IF NOT EXISTS films (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  RELEASE_DATE DATE NOT NULL,
  duration INTEGER NOT NULL,
  mpa INTEGER REFERENCES MPA_Ratings(MPARating_id)
);


CREATE TABLE IF NOT EXISTS genres (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS film_genres (
  film_id INT REFERENCES films(id) ON DELETE CASCADE,
  genre_id INT REFERENCES genres(genre_id) ON DELETE CASCADE,
  PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friendships (
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  friend_id INT REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, friend_id),
  CONSTRAINT fk_user_friend CHECK (user_id <> friend_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  film_id INT REFERENCES films(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id INT NOT NULL,
    film_id INT NOT NULL,
    useful INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_likes (
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Таблица для хранения режиссёров
CREATE TABLE IF NOT EXISTS directors (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Уникальный идентификатор режиссёра
  name VARCHAR(255) NOT NULL UNIQUE CHECK (LENGTH(name) > 0) -- Уникальное имя, не может быть пустым
);

-- Таблица для связи фильмов и режиссёров (многие ко многим)
CREATE TABLE IF NOT EXISTS film_directors (
  film_id INT NOT NULL, -- ID фильма
  directors_id INT NOT NULL, -- ID режиссёра
  PRIMARY KEY (film_id, directors_id), -- Уникальная комбинация фильм-режиссёр
  CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE, -- Удаление всех записей при удалении фильма
  CONSTRAINT fk_director FOREIGN KEY (directors_id) REFERENCES directors (id) ON DELETE CASCADE -- Удаление всех записей при удалении режиссёра
);

CREATE TABLE IF NOT EXISTS user_feed (
    event_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL, -- LIKE, REVIEW, FRIEND
    operation VARCHAR(50) NOT NULL,  -- ADD, REMOVE, UPDATE
    entity_id INTEGER NOT NULL,      -- ID сущности (фильм, отзыв, пользователь)
    timestamp BIGINT NOT NULL        -- Время события в формате Unix timestamp
);