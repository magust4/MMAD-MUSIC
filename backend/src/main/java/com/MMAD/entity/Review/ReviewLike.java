package com.MMAD.entity.Review;

import java.time.LocalDateTime;

import com.MMAD.entity.User.User;

import jakarta.persistence.*;

@Entity
@Table(
    name = "review_like",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "review_id"}
        )
    }
)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "review_id",
        nullable = false
    )
    private Review review;


    private LocalDateTime createdAt;


    public ReviewLike() {
    }


    public ReviewLike(User user, Review review) {
        this.user = user;
        this.review = review;
        this.createdAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }


    public User getUser() {
        return user;
    }


    public Review getReview() {
        return review;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}