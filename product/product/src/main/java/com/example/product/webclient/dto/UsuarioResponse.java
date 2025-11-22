package com.example.product.webclient.dto;

import lombok.Data;

@Data
public class UsuarioResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
}