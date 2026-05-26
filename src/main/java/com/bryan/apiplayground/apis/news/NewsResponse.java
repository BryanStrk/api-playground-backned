package com.bryan.apiplayground.apis.news;

import java.util.List;

public record NewsResponse(int totalResults, List<Article> articles) {
}
