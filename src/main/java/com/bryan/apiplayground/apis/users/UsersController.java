package com.bryan.apiplayground.apis.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Demos", description = "Usuarios falsos vía RandomUser (sin API key)")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/random")
    @Operation(
            summary = "Usuario aleatorio",
            description = "Devuelve un usuario falso para mockear UIs. El parámetro nat acepta códigos ISO 3166-1 alfa-2 (es, us, fr…)."
    )
    public UserResponse getRandom(@RequestParam(defaultValue = "es") String nat) {
        return usersService.getRandom(nat);
    }
}
