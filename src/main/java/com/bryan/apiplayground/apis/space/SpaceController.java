package com.bryan.apiplayground.apis.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/space")
@Tag(name = "Ciencia", description = "Astronomy Picture of the Day vía NASA (NASA_API_KEY opcional, DEMO_KEY por defecto)")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping("/apod")
    @Operation(
            summary = "Astronomy Picture of the Day",
            description = "Devuelve la imagen o vídeo astronómico del día con título, explicación y URLs (estándar y HD)."
    )
    public ApodResponse getApod() {
        return spaceService.getApod();
    }
}
