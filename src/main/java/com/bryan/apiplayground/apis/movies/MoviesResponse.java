package com.bryan.apiplayground.apis.movies;

import java.util.List;

public record MoviesResponse(
        int page,
        List<Movie> movies,
        int totalPages,
        int totalResults
) {
}
