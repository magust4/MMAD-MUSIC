package com.MMAD.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.MMAD.Service.s3.S3Service;
import com.MMAD.entity.User.User;
import com.MMAD.Service.user.UserService;
import com.MMAD.dto.MessageResponse;
import com.MMAD.dto.user.ForgotPasswordRequest;
import com.MMAD.dto.user.LoginRequest;
import com.MMAD.dto.user.RegisterRequest;
import com.MMAD.dto.user.ResendCodeRequest;
import com.MMAD.dto.user.ResetPasswordRequest;
import com.MMAD.dto.user.UserDTO;
import com.MMAD.dto.user.VerifyRequest;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

    /**
     * Constructor for UserResource.
     * 
     * @param userService The UserService to be used.
     */
    public UserController(
            UserService userService,
            S3Service s3Service) {

        this.userService = userService;
        this.s3Service = s3Service;
    }

    /**
     * Get a user by their id.
     * 
     * @param id The id of the user to be retrieved.
     * @return The user with the given id.
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        try {
            UserDTO user = userService.getUserDTOById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get a user by their username.
     * 
     * @param username The username of the user to be retrieved.
     * @return The user with the given username.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        try {
            UserDTO user = userService.getUserDTOByUsername(username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get a user by their username and password.
     * 
     * @param username The username of the user to be retrieved.
     * @param password The password of the user to be retrieved.
     * @return The user with the given username and password.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {

            return ResponseEntity.ok(
                    userService.login(
                            request.getLogin(),
                            request.getPassword()));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new MessageResponse(
                            false,
                            e.getMessage()));

        }
    }

    /**
     * Create a new user.
     * 
     * @param user The user to be created.
     * @return The created user DTO if successful.
     */
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> addUser(
            @RequestBody RegisterRequest request) {

        try {

            userService.createUser(
                    request.username(),
                    request.email(),
                    request.password());

            return ResponseEntity.ok(
                    new MessageResponse(
                            true,
                            "Account created successfully. Please check your email for your verification code."));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new MessageResponse(
                            false,
                            e.getMessage()));

        }
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {

        try {

            // -----------------------------
            // Validate file
            // -----------------------------

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Please select an image.");
            }

            // 5 MB maximum
            long maxFileSize = 5 * 1024 * 1024;

            if (file.getSize() > maxFileSize) {

                return ResponseEntity
                        .badRequest()
                        .body("Profile picture must be smaller than 5 MB.");
            }

            String contentType = file.getContentType();

            if (contentType == null ||
                    !contentType.startsWith("image/")) {

                return ResponseEntity
                        .badRequest()
                        .body("Only image files are allowed.");
            }

            // -----------------------------
            // Get authenticated user
            // -----------------------------

            String username = authentication.getName();

            User user = userService
                    .getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Save the old profile picture
            String oldProfilePicUrl = user.getProfilePicUrl();

            // -----------------------------
            // Upload new picture
            // -----------------------------

            String newFileKey = s3Service.uploadProfilePicture(file);

            // -----------------------------
            // Update database
            // -----------------------------

            user.setProfilePicUrl(newFileKey);

            userService.saveUser(user);

            // -----------------------------
            // Delete old S3 picture
            // -----------------------------

            if (oldProfilePicUrl != null &&
                    oldProfilePicUrl.startsWith(
                            "profile-pictures/")) {

                try {

                    s3Service.deleteProfilePicture(
                            oldProfilePicUrl);

                } catch (Exception e) {

                    // The new picture is already saved,
                    // so don't fail the request if cleanup fails.

                    System.err.println(
                            "Failed to delete old profile picture: "
                                    + e.getMessage());
                }
            }

            return ResponseEntity.ok(newFileKey);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload profile picture.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verifyUser(
            @RequestBody VerifyRequest request) {

        try {

            userService.verifyUser(
                    request.email(),
                    request.code());

            return ResponseEntity.ok(
                    new MessageResponse(
                            true,
                            "Account verified successfully"));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new MessageResponse(
                            false,
                            e.getMessage()));

        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @RequestBody ResendCodeRequest request) {

        try {

            userService.resendVerificationCode(
                    request.email());

            return ResponseEntity.ok(
                    new MessageResponse(
                            true,
                            "Verification code sent again."));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(
                            new MessageResponse(
                                    false,
                                    e.getMessage()));

        }

    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUserDTOByUsername(username));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        userService.forgotPassword(
                request.email());

        return ResponseEntity.ok(
                new MessageResponse(
                        true,
                        "Password reset code sent."));

    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(
                request.email(),
                request.code(),
                request.newPassword());

        return ResponseEntity.ok(
                new MessageResponse(
                        true,
                        "Password updated successfully."));

    }

    /**
     * Delete a user by their id.
     * 
     * @param id The id of the user to be deleted.
     * @return A message indicating the success of the deletion.
     */
    @Transactional
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Follow a user (add to following list).
     * 
     * @param username       The username of the user who wants to follow another
     *                       user.
     * @param followUsername The username of the user to be followed.
     * @return A message indicating success.
     */
    @Transactional
    @PostMapping("/follow")
    public ResponseEntity<String> followUser(@RequestParam("username") String username,
            @RequestParam("followUsername") String followUsername) {
        try {
            userService.getUserByUsername(username);
            userService.getUserByUsername(followUsername);
            userService.followUser(username, followUsername); // this method must be updated to accept usernames
            return ResponseEntity.ok("User followed successfully");
        } catch (Exception e) {
            System.err.println("Error following user: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollowUser(@RequestParam("username") String username,
            @RequestParam("unfollowUsername") String unfollowUsername) {
        try {
            userService.getUserByUsername(username);
            userService.getUserByUsername(unfollowUsername);
            userService.unfollowUser(username, unfollowUsername); // implement this in your service
            return ResponseEntity.ok("User unfollowed successfully");
        } catch (Exception e) {
            System.err.println("Error unfollowing user: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get list of users that the user is following.
     * 
     * @param userId The id of the user.
     * @return List of UserDTO representing the users being followed.
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<List<String>> getFollowingList(@PathVariable("userId") Long userId) {
        try {
            List<String> following = userService.getFollowingList(userId);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            System.err.println("Error retrieving following list: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get list of followers of a user.
     * 
     * @param userId The id of the user.
     * @return List of UserDTO representing the followers.
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowersList(@PathVariable("userId") Long userId) {
        try {
            List<UserDTO> followers = userService.getFollowers(userId);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            System.err.println("Error retrieving followers list: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // /**
    // * Remove all followings of a user (stop following everyone).
    // *
    // * @param userId The id of the user.
    // * @return Success message.
    // */
    // @PostMapping("/following/removeAll/{userId}")
    // public ResponseEntity<String> removeAllFollowing(@PathVariable("userId") Long
    // userId) {
    // try {
    // userService.removeAllFollowing(userId);
    // return ResponseEntity.ok("All followings removed successfully");
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // /**
    // * Remove all followers of a user (remove all followers).
    // *
    // * @param userId The id of the user.
    // * @return Success message.
    // */
    // @PostMapping("/followers/removeAll/{userId}")
    // public ResponseEntity<String> removeAllFollowers(@PathVariable("userId") Long
    // userId) {
    // try {
    // userService.removeAllFollowers(userId);
    // return ResponseEntity.ok("All followers removed successfully");
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // --- NEW ENDPOINT FOR SEARCHING USERS ---
    /**
     * Searches for users whose usernames contain the given query string
     * (case-insensitive).
     *
     * @param query The string to search for within usernames.
     * @return A list of UserDTOs matching the search criteria.
     */

    // --- NEW ENDPOINT FOR SEARCHING USERS (Similar to removeAllFriends) ---
    /**
     * Searches for users whose usernames contain the given query string
     * (case-insensitive).
     *
     * @param query The string to search for within usernames.
     * @return A list of UserDTOs matching the search criteria.
     */
    // @GetMapping("/search")
    // public ResponseEntity<List<UserItemDTO>> searchUsers(@RequestParam("query")
    // String query) {
    // try {
    // List<UserItemDTO> users = userService.searchUsers(query);
    // // If no users are found, userService.searchUsers returns an empty list,
    // // which is a valid success response (HTTP 200 OK with an empty array).
    // return ResponseEntity.ok(users);
    // } catch (Exception e) {
    // // Catch any unexpected exceptions that might occur during the service call
    // // and return a bad request with the error message, similar to your template.
    // return ResponseEntity.badRequest().body(null); // Returning null body with
    // badRequest indicates an error.
    // // You might prefer to return e.getMessage() here
    // // or a more specific error response.
    // }
    // }

}
