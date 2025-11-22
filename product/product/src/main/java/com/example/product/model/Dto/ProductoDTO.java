package com.example.product.model.Dto;

import lombok.Data;
import java.util.Base64;

@Data
public class ProductoDTO {
    private Long userId;
    private String token;
    private String name;
    private String description;
    private float price;
    private int stock;
    private String photo; 

    public byte[] getPhotoBytes() {
        if (photo == null || photo.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(photo);
    }
}