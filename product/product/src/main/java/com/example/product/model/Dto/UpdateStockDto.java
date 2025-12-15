package com.example.product.model.Dto;

import com.example.product.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStockDto {
    private String status; 
    private Product producto; 

}
