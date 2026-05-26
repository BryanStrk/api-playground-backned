package com.bryan.apiplayground.apis.movies;

public record Movie(
        int id,
        String title,
        String originalTitle,
        String overview,
        String releaseDate,
        String posterUrl,
        String backdropUrl,
        double voteAverage,
        int voteCount
) {
}
