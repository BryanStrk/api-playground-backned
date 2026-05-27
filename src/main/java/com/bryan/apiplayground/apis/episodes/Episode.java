package com.bryan.apiplayground.apis.episodes;

import java.util.List;

public record Episode(
        int id,
        String name,
        String episode,
        String airDate,
        List<Integer> characterIds
) {
}
