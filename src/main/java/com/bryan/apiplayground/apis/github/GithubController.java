package com.bryan.apiplayground.apis.github;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/github")
@Tag(name = "Desarrollo", description = "Perfil público de GitHub (sin API key, rate limit anónimo de 60/h)")
public class GithubController {

    private final GithubService githubService;

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Perfil público por usuario",
            description = "Devuelve nombre, bio, avatar, repos públicos y métricas de seguidores."
    )
    public GithubUserResponse getUser(@PathVariable String username) {
        return githubService.getUser(username);
    }
}
