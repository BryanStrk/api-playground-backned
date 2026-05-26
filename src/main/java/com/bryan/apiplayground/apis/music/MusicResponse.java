package com.bryan.apiplayground.apis.music;

import java.util.List;

public record MusicResponse(int resultCount, List<Track> tracks) {
}
