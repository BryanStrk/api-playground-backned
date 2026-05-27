package com.bryan.apiplayground.apis.holidays;

import com.fasterxml.jackson.annotation.JsonAlias;

public record Country(@JsonAlias("countryCode") String code, String name) {
}
