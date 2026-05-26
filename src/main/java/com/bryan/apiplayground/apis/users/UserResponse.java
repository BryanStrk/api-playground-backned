package com.bryan.apiplayground.apis.users;

public record UserResponse(
        String fullName,
        String email,
        String gender,
        String nationality,
        String pictureUrl,
        String city,
        String country
) {
}
