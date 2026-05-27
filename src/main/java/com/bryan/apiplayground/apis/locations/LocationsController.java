package com.bryan.apiplayground.apis.locations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Tag(name = "Entretenimiento", description = "Localizaciones de Rick and Morty (sin API key)")
public class LocationsController {

    private final LocationsService locationsService;

    public LocationsController(LocationsService locationsService) {
        this.locationsService = locationsService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar localizaciones por nombre",
            description = "Devuelve la lista de localizaciones cuyo nombre contiene el término. residentCount es el "
                    + "tamaño del array residents del upstream — útil para ordenar por 'más poblado' sin segundo request."
    )
    public List<Location> search(@RequestParam String q) {
        return locationsService.search(q);
    }
}
