package com.MMAD.dto.review;

import java.time.LocalDateTime;

import com.MMAD.dto.item.ItemDTO;
import com.MMAD.entity.Review.Review;

public class GetReviewResponse {

    private Long id;
    private int rating;
    private String description;

    private ItemDTO item;
    private String username;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long likeCount;
    private boolean likedByCurrentUser;

    public GetReviewResponse(
            Long id,
            int rating,
            String description,
            ItemDTO item,
            String username,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long likeCount,
            boolean likedByCurrentUser) {

        this.id = id;
        this.rating = rating;
        this.description = description;
        this.item = item;
        this.username = username;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public static GetReviewResponse fromEntity(
            Review review,
            long likeCount,
            boolean likedByCurrentUser) {

        if (review == null) {
            return null;
        }

        return new GetReviewResponse(
                review.getId(),
                review.getRating(),
                review.getDescription(),
                ItemDTO.fromEntity(review.getItem()),
                review.getUser().getUsername(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                likeCount,
                likedByCurrentUser);
    }
}