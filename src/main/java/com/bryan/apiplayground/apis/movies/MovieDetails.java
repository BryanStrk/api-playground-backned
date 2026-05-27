package com.bryan.apiplayground.apis.movies;

import java.util.List;

public record MovieDetails(
        int id,
        String title,
        String originalTitle,
        String overview,
        String tagline,
        String releaseDate,
        String posterUrl,
        String backdropUrl,
        double voteAverage,
        int voteCount,
        Integer runtime,
        String status,
        String homepage,
        String originalLanguage,
        List<Genre> genres
) {
}
