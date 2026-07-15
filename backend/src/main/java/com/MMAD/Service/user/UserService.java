package com.MMAD.Service.user;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.MMAD.Security.JWTService;
import com.MMAD.Service.user.EmailService;
import com.MMAD.dto.user.LoginResponse;
import com.MMAD.dto.user.UserDTO;
import com.MMAD.dto.user.UserDTOMapper;
import com.MMAD.entity.User.User;
import com.MMAD.exception.InvalidCodeException;
import com.MMAD.exception.UserNotFoundException;
import com.MMAD.repo.UserRepo;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

        private final UserRepo userRepo;
        private final PasswordEncoder passwordEncoder;
        private final JWTService jwtService;
        private final UserDTOMapper userDTOMapper;
        private final EmailService emailService;

        private static final Set<String> RESERVED_USERNAMES = new HashSet<>(
                        Arrays.asList(
                                        "admin",
                                        "administrator",
                                        "support",
                                        "help",
                                        "system",
                                        "root",
                                        "mmad",
                                        "mmadmusic",
                                        "mmad_music",
                                        "official",
                                        "moderator",
                                        "moderator",
                                        "api",
                                        "staff",
                                        "security",
                                        "contact",
                                        "billing",
                                        "music",
                                        "artist",
                                        "artists"));

        public UserService(
                        UserRepo userRepo,
                        JWTService jwtService,
                        PasswordEncoder passwordEncoder,
                        UserDTOMapper userDTOMapper,
                        EmailService emailService) {
                if (userRepo == null) {
                        throw new RuntimeException("userRepo cannot be null");
                }
                if (passwordEncoder == null) {
                        throw new RuntimeException("passwordEncoder cannot be null");
                }
                if (emailService == null) {
                        throw new RuntimeException("emailService cannot be null");
                }
                this.userRepo = userRepo;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.userDTOMapper = userDTOMapper;
                this.emailService = emailService;
        }

        @Transactional
        public UserDTO getCurrentUser() {
                String username = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                return getUserDTOByUsername(username);
        }

        @Transactional
        public User getCurrentUserEntity() {

                String username = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                return userRepo.findUserByUsername(username)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "User not found"));
        }

        @Transactional
        public UserDTO getUserDTOById(Long id) {
                return userRepo.findUserById(id)
                                .map(userDTOMapper::apply)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "user with id [%s] not found".formatted(id)));
        }

        @Transactional
        public UserDTO getUserDTOByUsername(String username) {
                return userRepo.findUserByUsername(username)
                                .map(userDTOMapper::apply)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "user with username [%s] not found".formatted(username)));
        }

        @Transactional
        public Optional<User> getUserByUsername(String username) {
                return userRepo.findUserByUsername(username);
        }

        public LoginResponse login(String username, String password) {
                User user = userRepo.findUserByUsername(username)
                                .orElseGet(() -> userRepo.findUserByEmail(username)
                                                .orElseThrow(() -> new RuntimeException("User not found")));
                if (!passwordEncoder.matches(password, user.getPassword())) {
                        throw new RuntimeException("Invalid credentials");
                }
                if (!user.isVerified()) {
                        throw new RuntimeException("Account not verified");
                }
                String token = jwtService.generateToken(user.getUsername());
                return new LoginResponse(
                                token,
                                user.getUsername());
        }

        public void createUser(
                        String username,
                        String email,
                        String password) {

                username = username.trim();
                email = email.trim().toLowerCase();

                validateUsername(username);

                Optional<User> existingUsername = userRepo.findUserByUsernameIgnoreCase(username);

                if (existingUsername.isPresent()) {
                        throw new RuntimeException(
                                        "Username already exists");
                }

                Optional<User> existingEmail = userRepo.findUserByEmailIgnoreCase(email);

                if (existingEmail.isPresent()) {
                        throw new RuntimeException(
                                        "Email already exists");
                }

                validatePassword(password);

                User newUser = new User(
                                username,
                                passwordEncoder.encode(password),
                                email);

                //newUser.setVerified(false);
//Email verification
                newUser.setVerified(true);

                String code = generateVerificationCode();

                newUser.setVerificationCode(code);

                LocalDateTime now = LocalDateTime.now();

                newUser.setVerificationCodeExpiry(
                                now.plusMinutes(15));

                newUser.setVerificationCodeSentAt(
                                now);

                userRepo.save(newUser);
                
                
//turn back on for email verification

                // try {

                //         emailService.sendVerificationEmail(
                //                         email,
                //                         code);

                // } catch (Exception e) {

                //         e.printStackTrace();

                //         throw new RuntimeException(
                //                         "Verification email failed: " + e.getMessage());

                // }
        }

        public void verifyUser(
                        String email,
                        String code) {

                User user = userRepo.findUserByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

                if (user.getVerificationCode() == null ||
                                !user.getVerificationCode().equals(code)) {

                        throw new InvalidCodeException(
                                        "Invalid verification code");

                }

                if (user.getVerificationCodeExpiry() == null ||
                                user.getVerificationCodeExpiry()
                                                .isBefore(LocalDateTime.now())) {

                        throw new InvalidCodeException(
                                        "Verification code expired");

                }
                user.setVerified(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiry(null);
                userRepo.save(user);
        }

        public void deleteUser(Long id) {

                User user = userRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User does not exist"));

                user.getFollowers().forEach(follower -> {
                        follower.getFollowing().remove(user);
                        userRepo.save(follower);
                });

                user.getFollowing().forEach(followingUser -> {
                        followingUser.getFollowers().remove(user);
                        userRepo.save(followingUser);
                });

                user.getFollowers().clear();
                user.getFollowing().clear();

                userRepo.delete(user);
        }

        public void followUser(
                        String username,
                        String toFollowUsername) {

                if (username.equals(toFollowUsername)) {
                        throw new IllegalArgumentException(
                                        "Cannot follow yourself");
                }

                User user = userRepo.findUserByUsername(username)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "User not found"));

                User toFollow = userRepo.findUserByUsername(toFollowUsername)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "User to follow not found"));

                if (user.getFollowing().contains(toFollow)) {
                        throw new IllegalArgumentException(
                                        "Already following this user");
                }

                user.getFollowing().add(toFollow);
                toFollow.getFollowers().add(user);

                userRepo.save(user);
                userRepo.save(toFollow);
        }

        public void unfollowUser(
                        String username,
                        String toUnfollowUsername) {

                User user = userRepo.findUserByUsername(username)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "User not found"));

                User toUnfollow = userRepo.findUserByUsername(toUnfollowUsername)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "User not found"));

                user.getFollowing().remove(toUnfollow);
                toUnfollow.getFollowers().remove(user);

                userRepo.save(user);
                userRepo.save(toUnfollow);
        }

        public List<String> getFollowingList(Long userId) {

                User user = userRepo.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "User not found"));

                return user.getFollowing()
                                .stream()
                                .map(User::getUsername)
                                .collect(Collectors.toList());
        }

        public List<UserDTO> getFollowers(Long userId) {

                User user = userRepo.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "User not found"));

                return user.getFollowers()
                                .stream()
                                .map(userDTOMapper::apply)
                                .collect(Collectors.toList());
        }

        public List<UserDTO> searchUsers(String query) {

                if (query == null || query.trim().isEmpty()) {
                        return List.of();
                }

                return userRepo.findUserByUsernameIgnoreCase(query)
                                .stream()
                                .map(userDTOMapper::apply)
                                .collect(Collectors.toList());
        }

        private String generateVerificationCode() {

                return String.valueOf(
                                (int) (Math.random() * 900000) + 100000);
        }

        public void forgotPassword(String email) {

                User user = userRepo.findUserByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                LocalDateTime now = LocalDateTime.now();

                if (user.getPasswordResetCodeSentAt() != null) {

                        LocalDateTime availableTime = user.getPasswordResetCodeSentAt()
                                        .plusSeconds(60);

                        if (now.isBefore(availableTime)) {

                                long secondsLeft = java.time.Duration
                                                .between(now, availableTime)
                                                .getSeconds();

                                throw new RuntimeException(
                                                "Please wait "
                                                                + secondsLeft
                                                                + " seconds before requesting another reset code.");

                        }

                }

                String code = generateVerificationCode();

                user.setPasswordResetCode(code);

                user.setPasswordResetCodeExpiry(
                                now.plusMinutes(15));

                user.setPasswordResetCodeSentAt(
                                now);

                userRepo.save(user);

//EMAIL VERIFICATION
                // emailService.sendPasswordResetEmail(
                //                 email,
                //                 code);

        }

        public void resetPassword(
                        String email,
                        String code,
                        String newPassword) {

                User user = userRepo.findUserByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getPasswordResetCode() == null ||
                                !user.getPasswordResetCode().equals(code)) {

                        throw new InvalidCodeException(
                                        "Invalid reset code");

                }
                if (user.getPasswordResetCodeExpiry() == null ||
                                user.getPasswordResetCodeExpiry()
                                                .isBefore(LocalDateTime.now())) {

                        throw new InvalidCodeException(
                                        "Password reset code expired");

                }

                validatePassword(newPassword);

                user.setPassword(
                                passwordEncoder.encode(newPassword));

                user.setPasswordResetCode(null);

                user.setPasswordResetCodeExpiry(null);

                user.setPasswordResetCodeSentAt(null);

                userRepo.save(user);

        }

        public void resendVerificationCode(String email) {

                User user = userRepo.findUserByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.isVerified()) {

                        throw new RuntimeException(
                                        "Account already verified");

                }

                LocalDateTime now = LocalDateTime.now();

                if (user.getVerificationCodeSentAt() != null) {

                        LocalDateTime availableTime = user.getVerificationCodeSentAt()
                                        .plusSeconds(60);

                        if (now.isBefore(availableTime)) {

                                long secondsLeft = java.time.Duration
                                                .between(now, availableTime)
                                                .getSeconds();

                                throw new RuntimeException(
                                                "Please wait "
                                                                + secondsLeft
                                                                + " seconds before requesting another code.");

                        }

                }

                String code = generateVerificationCode();

                user.setVerificationCode(code);

                user.setVerificationCodeExpiry(
                                now.plusMinutes(15));

                user.setVerificationCodeSentAt(
                                now);

                userRepo.save(user);

                emailService.sendVerificationEmail(
                                email,
                                code);

        }

        private void validatePassword(String password) {

                if (password == null || password.isBlank()) {
                        throw new RuntimeException(
                                        "Password cannot be empty.");
                }

                if (password.length() < 8) {
                        throw new RuntimeException(
                                        "Password must be at least 8 characters long.");
                }

                if (!password.matches(".*[A-Z].*")) {
                        throw new RuntimeException(
                                        "Password must contain at least one uppercase letter.");
                }

                if (!password.matches(".*[a-z].*")) {
                        throw new RuntimeException(
                                        "Password must contain at least one lowercase letter.");
                }

                if (!password.matches(".*\\d.*")) {
                        throw new RuntimeException(
                                        "Password must contain at least one number.");
                }

        }

        private void validateUsername(String username) {
                if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
                        throw new RuntimeException(
                                        "This username is reserved.");
                }

                if (username == null || username.isBlank()) {
                        throw new RuntimeException("Username cannot be empty.");
                }

                if (username.length() < 3 || username.length() > 20) {
                        throw new RuntimeException(
                                        "Username must be between 3 and 20 characters.");
                }

                if (username.contains("@")) {
                        throw new RuntimeException(
                                        "Username cannot contain '@'.");
                }

                if (!username.matches("^[A-Za-z0-9._]+$")) {
                        throw new RuntimeException(
                                        "Username may only contain letters, numbers, periods, and underscores.");
                }

                if (username.startsWith(".")
                                || username.endsWith(".")
                                || username.startsWith("_")
                                || username.endsWith("_")) {

                        throw new RuntimeException(
                                        "Username cannot start or end with '.' or '_'.");
                }

                if (username.contains("..") || username.contains("__")) {
                        throw new RuntimeException(
                                        "Username cannot contain consecutive '.' or '_'.");
                }
        }

}