package com.example.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Datos de inicio de sesión")
public class InicioSesion {
    @Schema(description = "email de usuario", required = true, example = "admin")
    private String mail;
    
    @Schema(description = "Contraseña del usuario", required = true, example = "password123")
    private String password;
}

