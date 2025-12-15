package com.example.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitarResetRequest {
    @Schema(description = "Correo del usuario que solicita el restablecimiento", example = "usuario@correo.com")
    private String correo;

}
