package com.example.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cart.model.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>{

}
