package com.bryan.apiplayground.apis.exchange;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
            description = "from = base ISO 4217 (por defecto EUR). to = lista coma-separada de símbolos a "
                    + "convertir (por defecto USD). Por ejemplo ?from=EUR&to=USD,GBP,JPY devuelve tres tasas."
    )
    public ExchangeResponse getLatest(
            @RequestParam(defaultValue = "EUR") String from,
            @RequestParam(defaultValue = "USD") String to
    ) {
        return exchangeService.getLatest(from, to);
    }

    @GetMapping("/currencies")
    @Operation(
            summary = "Lista de divisas soportadas",
            description = "Devuelve el catálogo completo de Frankfurter (~30 divisas ISO 4217) ordenado "
                    + "alfabéticamente por código, para poblar los selectores from/to del front."
    )
    public List<Currency> listCurrencies() {
        return exchangeService.listCurrencies();
    }
}
