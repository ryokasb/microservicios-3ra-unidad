package com.example.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    
    void deleteByIduser(Long iduser);

    List<Product> findAllByIduser(Long iduser);

    long countByIduser(Long iduser);

}