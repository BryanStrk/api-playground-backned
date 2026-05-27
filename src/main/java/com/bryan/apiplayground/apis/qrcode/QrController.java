package com.bryan.apiplayground.apis.qrcode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qrcode")
@Tag(name = "Herramientas", description = "Generador de códigos QR vía api.qrserver.com (sin API key)")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping
    @Operation(
            summary = "Genera la URL de un QR a partir de un texto",
            description = "Devuelve qrUrl listo para <img src>. data: texto o URL. size: formato WxH (default 300x300); "
                    + "valores inválidos caen al default."
    )
    public QrResponse generate(
            @RequestParam String data,
            @RequestParam(required = false) String size
    ) {
        return qrService.generate(data, size);
    }
}
