package com.example.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestablecerContrasenaRequest {

     @Schema(description = "Correo del usuario", example = "usuario@correo.com")
    private String correo;
    @Schema(description = "Codigo de recuperacion", example = "12345")
    private String codigo;

    @Schema(description = "Nueva contraseña", example = "nuevaPassword123")
    private String nuevaContrasena;

    @Schema(description = "Confirmación de nueva contraseña", example = "nuevaPassword123")
    private String confirmarContrasena;


}

