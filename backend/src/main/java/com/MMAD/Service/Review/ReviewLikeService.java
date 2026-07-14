package com.MMAD.Service.Review;

import org.springframework.stereotype.Service;

import com.MMAD.entity.Review.Review;
import com.MMAD.entity.Review.ReviewLike;
import com.MMAD.entity.User.User;
import com.MMAD.repo.UserRepo;
import com.MMAD.repo.Review.ReviewLikeRepo;
import com.MMAD.repo.Review.ReviewRepo;

@Service
public class ReviewLikeService {

        private final ReviewLikeRepo reviewLikeRepo;
        private final UserRepo userRepo;
        private final ReviewRepo reviewRepo;

        public ReviewLikeService(
                        ReviewLikeRepo reviewLikeRepo,
                        UserRepo userRepo,
                        ReviewRepo reviewRepo) {

                this.reviewLikeRepo = reviewLikeRepo;
                this.userRepo = userRepo;
                this.reviewRepo = reviewRepo;
        }

        public void likeReview(
                        Long userId,
                        Long reviewId) {
                if (reviewLikeRepo.existsByUserIdAndReviewId(
                                userId,
                                reviewId)) {

                        throw new RuntimeException(
                                        "Review already liked");
                }

                User user = userRepo.findById(userId)
                                .orElseThrow();

                Review review = reviewRepo.findById(reviewId)
                                .orElseThrow();

                ReviewLike like = new ReviewLike(
                                user,
                                review);

                ReviewLike saved = reviewLikeRepo.save(like);
        }

        public void unlikeReview(
                        Long userId,
                        Long reviewId) {

                ReviewLike like = reviewLikeRepo
                                .findByUserIdAndReviewId(
                                                userId,
                                                reviewId)
                                .orElseThrow();

                reviewLikeRepo.delete(like);
        }

        public long getLikeCount(Long reviewId) {

                return reviewLikeRepo.countByReviewId(reviewId);
        }

        public boolean hasLiked(
                        Long userId,
                        Long reviewId) {

                return reviewLikeRepo.existsByUserIdAndReviewId(
                                userId,
                                reviewId);
        }
}
