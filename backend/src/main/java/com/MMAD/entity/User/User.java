package com.MMAD.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String profilePicUrl;


    // -----------------------------
    // Email Verification
    // -----------------------------

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean verified = false;

    @Column
    private String verificationCode;

    @Column
    private String passwordResetCode;

    @Column
    private LocalDateTime verificationCodeSentAt;

    @Column
    private LocalDateTime verificationCodeExpiry;

    @Column
    private LocalDateTime passwordResetCodeExpiry;

    @Column
    private LocalDateTime passwordResetCodeSentAt;

    // -----------------------------
    // FOLLOWERS / FOLLOWING Logic
    // -----------------------------

    // Users this user is following
    @ManyToMany
    @JoinTable(name = "user_following", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "following_id"))
    private Set<User> following = new HashSet<>();

    // Users who follow this user
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    // -----------------------------
    // Other Relationships
    // -----------------------------

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Playlist> playlists = new ArrayList<>();

    // -----------------------------
    // Constructors
    // -----------------------------

    public User() {
    }

    public User(String username, String password, String email) {

        this.username = username;
        this.password = password;
        this.email = email;
        this.verified = false;

    }

    public User(
            Long id,
            String username,
            String password,
            String email) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.verified = false;

    }

    // -----------------------------
    // Getters & Setters
    // -----------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Username cannot be null or empty");
        } else if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException(
                    "Username length must be between 3 and 20 characters.");
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException(
                    "Username must contain only letters and numbers");
        } else {
            this.username = username;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }

    public String getPasswordResetCode() {
        return passwordResetCode;
    }

    public void setPasswordResetCode(String passwordResetCode) {
        this.passwordResetCode = passwordResetCode;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(LocalDateTime expiry) {
        this.verificationCodeExpiry = expiry;
    }

    public LocalDateTime getPasswordResetCodeExpiry() {
        return passwordResetCodeExpiry;
    }

    public void setPasswordResetCodeExpiry(LocalDateTime expiry) {
        this.passwordResetCodeExpiry = expiry;
    }

    public LocalDateTime getVerificationCodeSentAt() {
        return verificationCodeSentAt;
    }

    public void setVerificationCodeSentAt(LocalDateTime sentAt) {
        this.verificationCodeSentAt = sentAt;
    }

    public LocalDateTime getPasswordResetCodeSentAt() {
        return passwordResetCodeSentAt;
    }

    public void setPasswordResetCodeSentAt(LocalDateTime sentAt) {
        this.passwordResetCodeSentAt = sentAt;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    // -----------------------------
    // Helper Methods
    // -----------------------------

    public void follow(User target) {
        this.following.add(target);
        target.followers.add(this);
    }

    public void unfollow(User target) {
        this.following.remove(target);
        target.followers.remove(this);

    }

}