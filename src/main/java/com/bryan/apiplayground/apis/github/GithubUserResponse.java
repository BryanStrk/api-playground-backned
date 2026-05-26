package com.bryan.apiplayground.apis.github;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GithubUserResponse(
        String login,
        long id,
        String name,
        String company,
        String bio,
        @JsonAlias("avatar_url") String avatarUrl,
        @JsonAlias("html_url") String htmlUrl,
        @JsonAlias("public_repos") int publicRepos,
        int followers,
        int following
) {
}
