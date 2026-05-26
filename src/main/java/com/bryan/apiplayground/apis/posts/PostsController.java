package com.bryan.apiplayground.apis.posts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Demos", description = "Datos fake de JSONPlaceholder (sin API key)")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Post de ejemplo por id",
            description = "Útil para demos: devuelve userId, id, title y body."
    )
    public PostResponse getById(@PathVariable int id) {
        return postsService.getById(id);
    }
}
