package com.bryan.apiplayground.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiPlaygroundOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Playground")
                        .description("""
                                Spring Boot 4 backend that proxies 20 public APIs.
                                The frontend talks only to this backend, so API keys never reach the browser
                                and there are no third-party CORS blocks. Includes a concurrent /health endpoint
                                for live in-class monitoring demos.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bryan")
                                .email("bryanpaicoalbines97@gmail.com"))
                        .license(new License().name("MIT")));
    }
}
