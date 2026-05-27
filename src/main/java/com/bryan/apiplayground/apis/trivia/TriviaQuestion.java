package com.bryan.apiplayground.apis.trivia;

import java.util.List;

public record TriviaQuestion(
        String category,
        String type,
        String difficulty,
        String question,
        String correctAnswer,
        List<String> incorrectAnswers
) {
}
