package com.example.user.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.user.model.PasswordReset;

@Repository
public interface PasswordRecoveryRepository extends JpaRepository<PasswordReset, Long>{

    // Obtiene la última solicitud de recuperación de un usuario
    PasswordReset findTopByEmailOrderByCreatedAtDesc(String email);

    // Busca un registro por código (para validación)
    PasswordReset findByRecoveryCode(String recoveryCode);

    // Verifica si un código sigue siendo válido para un email concreto
    PasswordReset findByEmailAndRecoveryCode(String email, String recoveryCode);

    // Opcional: elimina peticiones viejas
    void deleteByExpiresAtBefore(LocalDateTime now);


}
