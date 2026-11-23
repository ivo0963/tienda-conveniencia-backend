package cl.duoc.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Documentación de la API en /swagger-ui.html. El botón "Authorize" recibe el token de acceso JWT. */
@Configuration
public class ConfiguracionOpenApi {

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API MiniMarket DSY")
                        .version("v1")
                        .description("""
                                API REST de la tienda MiniMarket DSY (DSY1104 Evaluación Parcial 3).

                                **Cómo probar los endpoints protegidos:**
                                1. Ejecuta `POST /api/v1/auth/login` con `admin@duoc.cl` / `admin123`.
                                2. Copia el valor de `token` de la respuesta.
                                3. Presiona **Authorize** y pégalo (sin la palabra Bearer).

                                Cuentas de prueba: administrador `admin@duoc.cl` / `admin123`, \
                                vendedor `vendedor@profesor.duoc.cl` / `vende123`, cliente `juan.perez@gmail.com` / `juan1234`.""")
                        .contact(new Contact().name("Ivo Martínez").url("https://github.com/ivo0963")))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token de acceso obtenido en /api/v1/auth/login")));
    }
}
