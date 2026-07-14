package com.MMAD.Controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.MMAD.Service.Review.ReviewService;
import com.MMAD.Service.Review.ReviewLikeService;
import com.MMAD.Service.user.UserService;
import com.MMAD.dto.review.GetReviewResponse;
import com.MMAD.dto.review.ItemReviewsResponse;
import com.MMAD.dto.review.PostReviewRequest;
import com.MMAD.dto.review.UpdateReviewRequest;
import com.MMAD.entity.User.User;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final UserService userService;

    public ReviewController(
            ReviewService reviewService,
            ReviewLikeService reviewLikeService,
            UserService userService) {

        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.userService = userService;
    }

    // CREATE
    @PostMapping("/add")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody PostReviewRequest reviewRequest) {

        try {

            GetReviewResponse savedReview = reviewService.createReview(
                    reviewRequest.getItemId(),
                    reviewRequest.getRating(),
                    reviewRequest.getDescription());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedReview);

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<?> likeReview(
            @PathVariable Long reviewId) {

        System.out.println("LIKE REQUEST RECEIVED: " + reviewId);

        User user = userService.getCurrentUserEntity();

        System.out.println("USER: " + user.getId());

        reviewLikeService.likeReview(
                user.getId(),
                reviewId);

        return ResponseEntity.ok().build();
    }

    // UNLIKE REVIEW
    @DeleteMapping("/{reviewId}/like")
    public ResponseEntity<?> unlikeReview(
            @PathVariable Long reviewId) {

        User user = userService.getCurrentUserEntity();

        reviewLikeService.unlikeReview(
                user.getId(),
                reviewId);

        return ResponseEntity.ok().build();
    }

    // READ ALL REVIEWS
    @GetMapping("/all")
    public ResponseEntity<List<GetReviewResponse>> getAllReviews() {

        try {

            List<GetReviewResponse> reviews = reviewService.getAllReviews();

            return ResponseEntity.ok(reviews);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("find/{id}")
    public ResponseEntity<GetReviewResponse> getReviewById(
            @PathVariable("id") Long id) {

        try {

            GetReviewResponse review = reviewService.getReviewById(id);

            return ResponseEntity.ok(review);

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<GetReviewResponse>> getReviewsByUserId(
            @PathVariable String username) {

        try {

            List<GetReviewResponse> reviews = reviewService.getReviewsByUsername(username);

            return ResponseEntity.ok(reviews);

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/me/item/{itemId}")
    public ResponseEntity<?> getMyReviewForItem(
            @PathVariable Long itemId) {

        return reviewService
                .getMyReviewForItem(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<ItemReviewsResponse> getReviewsByItemId(
            @PathVariable Long itemId) {

        try {

            ItemReviewsResponse response = reviewService.getReviewsByItemId(itemId);

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest updateRequest) {

        try {

            GetReviewResponse updatedReview = reviewService.updateReview(
                    id,
                    updateRequest.getRating(),
                    updateRequest.getDescription());

            return ResponseEntity.ok(updatedReview);

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<List<GetReviewResponse>> getReviewFeed() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<GetReviewResponse> reviews = reviewService.getFeedReviews(username);

        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long id) {

        try {

            reviewService.deleteReview(id);

            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test")
    public String test() {

        return "Review API is working";
    }
}