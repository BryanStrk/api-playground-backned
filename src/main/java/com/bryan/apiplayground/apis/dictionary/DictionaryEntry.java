package com.bryan.apiplayground.apis.dictionary;

import java.util.List;

public record DictionaryEntry(String word, String phonetic, List<Meaning> meanings) {
}
