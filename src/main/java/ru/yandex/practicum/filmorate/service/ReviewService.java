package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserFeedEventService userFeedEventService;

    public ReviewService(ReviewStorage reviewStorage, UserFeedEventService userFeedEventService) {
        this.reviewStorage = reviewStorage;
        this.userFeedEventService = userFeedEventService;
    }

    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Adding review: {}", review);
        validateReview(review);
        Review addedReview = reviewStorage.addReview(review);
        log.info("Review added successfully: {}", addedReview);

//        addUserEvent(review.getUserId(), "REVIEW", "ADD", review.getFilmId());
        addUserEvent(review.getUserId(), "REVIEW", "ADD", addedReview.getReviewId());

        return addedReview;
    }

    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Updating review: {}", review);
        if (review.getReviewId() == null) {
            log.error("Review ID is null. Validation failed.");
            throw new ValidationException("Review ID cannot be null for update.");
        }
        validateReview(review);
        Review updatedReview = reviewStorage.updateReview(review);
        log.info("Review updated successfully: {}", updatedReview);

//        addUserEvent(review.getUserId(), "REVIEW", "UPDATE", review.getFilmId());
        addUserEvent(review.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId());

        return updatedReview;
    }

    public void deleteReview(Integer reviewId) {
        log.info("Deleting review with ID: {}", reviewId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }

        int userId = getReviewById(reviewId).getUserId();
        int filmId = getReviewById(reviewId).getFilmId();

        reviewStorage.deleteReview(reviewId);
        log.info("Review with ID {} deleted successfully.", reviewId);

//        addUserEvent(userId, "REVIEW", "REMOVE", filmId);
        addUserEvent(userId, "REVIEW", "REMOVE", reviewId);
    }

    public Review getReviewById(Integer reviewId) {
        log.info("Fetching review with ID: {}", reviewId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        Review review = reviewStorage.getReviewById(reviewId);
        log.info("Fetched review: {}", review);
        return review;
    }

    public List<Review> getReviews(Integer filmId, Integer count) {
        log.info("Fetching reviews for film ID: {}, count: {}", filmId, count);
        if (filmId  == null && (count == null || count <= 0)) {
            count = 0; // Значение по умолчанию
            log.info("Count not specified or invalid, defaulting to 0.");
        }
        List<Review> reviews = reviewStorage.getReviews(filmId, count);
        log.info("Fetched {} reviews.", reviews.size());
        return reviews;
    }

    public void addLike(Integer reviewId, Integer userId) {
        log.info("Adding like to review ID: {} by user ID: {}", reviewId, userId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.addLike(reviewId, userId);
        log.info("Like added to review ID: {} by user ID: {}", reviewId, userId);

        addUserEvent(userId, "LIKE", "ADD", reviewId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        log.info("Adding dislike to review ID: {} by user ID: {}", reviewId, userId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }

        boolean isLike = reviewStorage.isLiked(reviewId, userId);
        if (isLike) {
            addUserEvent(userId, "LIKE", "REMOVE", reviewId);
        }

        reviewStorage.addDislike(reviewId, userId);
        log.info("Dislike added to review ID: {} by user ID: {}", reviewId, userId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        log.info("Removing like from review ID: {} by user ID: {}", reviewId, userId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.removeLike(reviewId, userId);
        log.info("Like removed from review ID: {} by user ID: {}", reviewId, userId);

        addUserEvent(userId, "LIKE", "REMOVE", reviewId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        log.info("Removing dislike from review ID: {} by user ID: {}", reviewId, userId);
        if (!reviewStorage.existsById(reviewId)) {
            log.error("Review with ID {} not found.", reviewId);
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Dislike removed from review ID: {} by user ID: {}", reviewId, userId);
    }

    private void validateReview(@Valid @RequestBody Review review) {
        log.info("Validating review: {}", review);
        if (review.getContent() == null || review.getContent().isBlank()) {
            log.error("Validation failed: Review content is null or blank.");
            throw new ValidationException("Review content cannot be null or blank.");
        }
        if (review.getUserId() == null) {
            log.error("Validation failed: Review user ID is null.");
            throw new ValidationException("Review must have a valid user ID.");
        }
        if (review.getFilmId() == null) {
            log.error("Validation failed: Review film ID is null.");
            throw new ValidationException("Review must have a valid film ID.");
        }
        if (review.getIsPositive() == null) {
            log.error("Validation failed: Review isPositive value is invalid.");
            throw new ValidationException("Review must have a valid isPositive value (true or false).");
        }
    }

    private void addUserEvent(Integer userId, String eventType, String operation, Integer entityId) {
        log.info("Создание события типа \"{}\" для операции \"{}\", для пользователя с id = {}", eventType, operation, userId);
        UserFeedEvent event = new UserFeedEvent(
                0, // eventId будет сгенерирован базой данных
                userId,
                eventType,
                operation,
                entityId,
                Instant.now().toEpochMilli()
        );
        userFeedEventService.addUserEvent(event);
        log.info("Событие типа \"{}\" для операции \"{}\" для пользователя с id = {} внесено в БД", eventType, operation, userId);
    }
}