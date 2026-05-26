package com.bryan.apiplayground.apis.photos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/photos")
@Tag(name = "Fotografía", description = "Fotografías aleatorias vía Unsplash (requiere UNSPLASH_ACCESS_KEY)")
public class PhotosController {

    private final PhotosService photosService;

    public PhotosController(PhotosService photosService) {
        this.photosService = photosService;
    }

    @GetMapping("/random")
    @Operation(
            summary = "Foto aleatoria por término",
            description = "Devuelve una foto aleatoria filtrada por el término indicado, con URL regular y miniatura, autor y dimensiones."
    )
    public PhotoResponse getRandom(@RequestParam(defaultValue = "nature") String query) {
        return photosService.getRandom(query);
    }
}
