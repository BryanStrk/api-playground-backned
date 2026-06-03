package com.bryan.apiplayground.apis.spacedevs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spacedevs")
@Tag(name = "Ciencia", description = "Próximos lanzamientos espaciales vía The Space Devs / Launch Library 2 (sin API key)")
public class SpaceDevsController {

    private final SpaceDevsService spaceDevsService;

    public SpaceDevsController(SpaceDevsService spaceDevsService) {
        this.spaceDevsService = spaceDevsService;
    }

    @GetMapping("/launches")
    @Operation(
            summary = "Próximos lanzamientos",
            description = "Devuelve los próximos 10 lanzamientos con nombre, fecha (net), estado, "
                    + "proveedor, plataforma de lanzamiento e imagen. El upstream limita a 15 peticiones/hora "
                    + "por IP, así que la respuesta se cachea ~12 minutos en memoria para no agotar la cuota."
    )
    public LaunchesResponse getLaunches() {
        return spaceDevsService.getUpcomingLaunches();
    }
}
