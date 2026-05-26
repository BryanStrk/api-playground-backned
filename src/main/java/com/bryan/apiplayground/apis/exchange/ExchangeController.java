package com.bryan.apiplayground.apis.exchange;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange")
@Tag(name = "Finanzas", description = "Tipos de cambio vía Frankfurter (sin API key)")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping
    @Operation(
            summary = "Cambio actual entre divisas",
            description = "Por defecto consulta EUR → USD. Acepta cualquier código ISO 4217 de divisa."
    )
    public ExchangeResponse getLatest(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(defaultValue = "USD") String symbols
    ) {
        return exchangeService.getLatest(base, symbols);
    }
}
