package com.bryan.apiplayground.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Monitorización", description = "Ping concurrente a las 20 APIs externas para demos en clase")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    @Operation(
            summary = "Health check concurrente de las 20 APIs",
            description = "Lanza una petición en paralelo (virtual threads) a cada upstream y devuelve "
                    + "estado, código HTTP y latencia individual. totalTimeMs es el wall-clock total: "
                    + "si es próximo a la API más lenta y no a la suma, el paralelismo funciona. "
                    + "Las APIs con key REQUIRED no configurada aparecen como SKIPPED."
    )
    public HealthReport check() {
        return healthService.checkAll();
    }
}
