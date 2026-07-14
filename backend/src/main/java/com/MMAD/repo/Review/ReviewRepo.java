package com.MMAD.repo.Review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MMAD.dto.review.ItemReviewResponse;
import com.MMAD.entity.Review.Review;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

    @Query("""
            SELECT new com.MMAD.dto.review.ItemReviewResponse(
                r.id,
                r.rating,
                r.description,
                r.user.username,
                r.createdAt,
                r.updatedAt
            )
            FROM Review r
            WHERE r.item.id = :itemId
            ORDER BY COALESCE(r.updatedAt, r.createdAt) DESC
            """)
    List<ItemReviewResponse> findReviewResponsesByItemId(@Param("itemId") Long itemId);

    List<Review> findByUserIdOrderByIdDesc(Long userId);

    List<Review> findByRatingGreaterThanEqual(int minRating);

    Optional<Review> findByUserIdAndItemId(Long userId, Long itemId);

    @Query("""
                SELECT r
                FROM Review r
                WHERE r.user.id = :userId
                   OR r.user IN (
                        SELECT f
                        FROM User u
                        JOIN u.following f
                        WHERE u.id = :userId
                   )
                ORDER BY COALESCE(r.updatedAt, r.createdAt) DESC
            """)
    List<Review> findFeedReviews(@Param("userId") Long userId);
}
