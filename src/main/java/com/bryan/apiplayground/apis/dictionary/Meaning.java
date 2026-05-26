package com.bryan.apiplayground.apis.dictionary;

import java.util.List;

public record Meaning(String partOfSpeech, List<Definition> definitions) {
}
