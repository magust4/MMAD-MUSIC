package com.MMAD.dto.search;

import com.MMAD.dto.user.UserDTO;

public class UserSearchResultDTO extends SearchResultDTO {

    public UserSearchResultDTO(
            Long id,
            String username,
            String imageURL
    ) {
        super(username, imageURL, "user");
    }

    public static UserSearchResultDTO fromDTO(UserDTO user) {

        if (user == null) {
            return null;
        }

        return new UserSearchResultDTO(
                null,
                user.username(),
                user.profilePicUrl()
        );
    }
}