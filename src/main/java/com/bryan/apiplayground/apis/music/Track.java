package com.bryan.apiplayground.apis.music;

public record Track(
        long trackId,
        String trackName,
        String artistName,
        String collectionName,
        String previewUrl,
        String artworkUrl,
        int trackTimeMillis,
        String genre
) {
}
