package com.bryan.apiplayground.apis.space;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ApodResponse(
        String date,
        String title,
        String explanation,
        String url,
        @JsonAlias("hdurl") String hdUrl,
        @JsonAlias("media_type") String mediaType,
        String copyright
) {
}
