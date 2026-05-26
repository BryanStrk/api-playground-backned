package com.bryan.apiplayground.apis.posts;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostResponse(
        @JsonProperty("userId") int userId,
        int id,
        String title,
        String body
) {
}
