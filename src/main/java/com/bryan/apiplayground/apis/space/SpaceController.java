package com.bryan.apiplayground.apis.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            description = "Sin params devuelve la imagen del día. Acepta ?date=YYYY-MM-DD para un día concreto "
                    + "o ?random=true para una entrada aleatoria del archivo. random gana si llegan ambos."
    )
    public ApodResponse getApod(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "false") boolean random
    ) {
        if (random) {
            return spaceService.getRandomApod();
        }
        return spaceService.getApod(date);
    }
}
