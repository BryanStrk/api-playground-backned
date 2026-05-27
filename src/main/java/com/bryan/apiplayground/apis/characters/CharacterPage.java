package com.bryan.apiplayground.apis.characters;

import java.util.List;

public record CharacterPage(PageInfo info, List<CharacterSummary> results) {
}
