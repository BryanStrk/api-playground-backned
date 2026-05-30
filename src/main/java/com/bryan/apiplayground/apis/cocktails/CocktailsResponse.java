package com.bryan.apiplayground.apis.cocktails;

import java.util.List;

public record CocktailsResponse(List<Cocktail> cocktails) {
}
