package com.example.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Producto del sistema - Contiene la información básica y la foto del producto")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autoincrementable del producto")
    private long id;


    @Column(nullable = false)
    @Schema(description = "llave foranea de id user")
    private Long iduser;
    
    @Column(nullable = false)
    @Schema(description = "Nombre del producto", example = "Laptop Dell XPS 13")
    private String name;

    @Column(length = 500)
    @Schema(description = "Descripción del producto", example = "Laptop con procesador Intel i7 de 11ª generación")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Precio del producto", example = "1299.99")
    private double price;

    @Column(nullable = false)
    @Schema(description = "Cantidad disponible en stock", example = "50")
    private int stock;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGBLOB")
    @Schema(description = "Foto del producto en formato binario (LONGBLOB)")
    private byte[] photo;
}
