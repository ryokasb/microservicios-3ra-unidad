package com.example.cart.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  AddItemRequest {
private String token;
private Long userid;
private Long productid;
private int quantity;
}
