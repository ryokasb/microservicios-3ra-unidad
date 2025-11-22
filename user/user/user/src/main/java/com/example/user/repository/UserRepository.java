package com.example.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

 
    // Método existente para verificar username único
    boolean existsByUsername(String username);
    
    // Método existente para verificar correo único
    boolean existsByCorreo(String correo);

    // busca un usuario por su correo (retorna Optional para manejar nulls)
    Optional<User> findByCorreo(String correo);

    // Busca un usuario por su username (retorna Optional para manejar nulls)
    Optional<User> findByUsername(String username);

}
