package com.MMAD.repo.Review;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.MMAD.entity.Review.ReviewLike;

public interface ReviewLikeRepo
        extends JpaRepository<ReviewLike, Long> {

    boolean existsByUserIdAndReviewId(
            Long userId,
            Long reviewId);

    Optional<ReviewLike> findByUserIdAndReviewId(
            Long userId,
            Long reviewId);

    long countByReviewId(
            Long reviewId);
}