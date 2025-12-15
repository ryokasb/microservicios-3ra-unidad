package com.example.user.model;


import java.time.LocalDateTime;


import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recuperacion_contrasena")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad para gestionar solicitudes de recuperación de contraseña")
public class PasswordReset {

    @Schema(description = "ID autoincrementable")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Schema(description = "email de el usuario al que esta asignado")
    @Column
    private String email;

    @Schema(description = "fecha de creacion de la peticion de recuperacion")
    @Column
    private LocalDateTime createdAt;
    
    @Schema(description = "fecha de expiracion del codigo")
    @Column
    private LocalDateTime expiresAt;

    @Schema(description = "codigo de recuperacion de 5 digitos")
    @Column
    private String recoveryCode;



}
