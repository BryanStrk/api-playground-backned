package com.bryan.apiplayground.apis.music;

public record Track(
        Long trackId,
        String trackName,
        String artistName,
        String collectionName,
        String previewUrl,
        String artworkUrl,
        Integer trackTimeMillis,
        String genre
) {
}
