package com.bryan.apiplayground.apis.crypto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crypto")
@Tag(name = "Finanzas", description = "Precios de criptomonedas vía CoinGecko (sin API key)")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/price")
    @Operation(
            summary = "Precio actual de criptomonedas",
            description = "ids = lista separada por comas de coingecko ids (bitcoin,ethereum…). vs = divisa fiat (eur, usd…)."
    )
    public CryptoPriceResponse getPrice(
            @RequestParam String ids,
            @RequestParam(defaultValue = "eur") String vs
    ) {
        return cryptoService.getPrice(ids, vs);
    }
}
