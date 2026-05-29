package com.bryan.apiplayground.apis.worldcup;

import java.util.List;

public record WorldCupInfo(
        String name,
        int matchCount,
        String firstMatchDate,
        String lastMatchDate,
        List<String> grounds
) {
}
