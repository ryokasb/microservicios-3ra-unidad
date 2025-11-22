package com.example.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.user.model.Rol;


@Repository
public interface RoleRepository extends JpaRepository<Rol, Long>{
 
    Optional<Rol> findByNombre(String nombre);
}
