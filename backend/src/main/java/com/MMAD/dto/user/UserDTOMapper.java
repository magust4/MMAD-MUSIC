package com.MMAD.dto.user;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.MMAD.Service.s3.S3Service;
import com.MMAD.entity.User.User;

@Service
public class UserDTOMapper implements Function<User, UserDTO> {

    private final S3Service s3Service;

    public UserDTOMapper(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public UserDTO apply(User user) {

        String profilePicUrl = null;

        if (user.getProfilePicUrl() != null &&
            !user.getProfilePicUrl().isBlank()) {

            profilePicUrl = s3Service.generatePresignedUrl(
                user.getProfilePicUrl()
            );
        }

        return new UserDTO(
            user.getUsername(),
            profilePicUrl,
            user.getFollowing()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList()),
            user.getFollowers()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        );
    }
}