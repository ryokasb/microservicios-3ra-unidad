package com.example.user.model.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateResponse {

    @Schema(description = "id de usuario para la respuesta a actualizar",example = "1l")
    private long id;
    @Schema(description = "nombre de usuario para la respuesta a actualizar",example = "Ryoka")
    private String username;
    @Schema(description = "correo de usuario para la respuesta a actualizar",example = "ryoka@gmmail.com")
    private String correo;

}
