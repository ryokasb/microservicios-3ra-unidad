package com.example.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cart.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{

    Optional<Cart> findByUserId(Long userId);
}
