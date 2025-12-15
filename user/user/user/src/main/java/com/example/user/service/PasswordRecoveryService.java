package com.example.user.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.user.model.PasswordReset;
import com.example.user.repository.PasswordRecoveryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PasswordRecoveryService {

    @Autowired
    private PasswordRecoveryRepository repo;

    /**
     * Genera un código de recuperación de 5 dígitos
     */
    public String generarCodigo() {
        Random random = new Random();
        int number = 10000 + random.nextInt(90000); // entre 10000 y 99999
        return String.valueOf(number);
    }

    /**
     * Registra una nueva solicitud de recuperación para el email
     */
    public PasswordReset crearSolicitud(String email) {

        String codigo = generarCodigo();
        LocalDateTime ahora = LocalDateTime.now();

        PasswordReset reset = new PasswordReset();
        reset.setEmail(email);
        reset.setCreatedAt(ahora);
        reset.setExpiresAt(ahora.plusMinutes(10)); // expira en 10 minutos
        reset.setRecoveryCode(codigo);

        return repo.save(reset);
    }

    /**
     * Obtiene la última solicitud de recuperación hecha por ese email
     */
    public PasswordReset obtenerUltimaSolicitud(String email) {
        return repo.findTopByEmailOrderByCreatedAtDesc(email);
    }

    /**
     * Verifica si el código ingresado por el usuario es correcto
     */
    public boolean validarCodigo(String email, String codigo) {

        PasswordReset registro = repo.findByEmailAndRecoveryCode(email, codigo);

        if (registro == null) {
            return false; // no existe ese código para ese email
        }

        // Verificar expiración
        if (registro.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false; // ya expiró
        }

        return true;
    }

}
