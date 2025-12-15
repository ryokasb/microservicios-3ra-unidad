package com.example.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.user.model.PasswordReset;
import com.example.user.repository.PasswordRecoveryRepository;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @InjectMocks
    private PasswordRecoveryService service;

    @Mock
    private PasswordRecoveryRepository repo;

    // ================= GENERAR CÓDIGO =================

    @Test
    void generarCodigo_5Digitos() {
        String codigo = service.generarCodigo();

        assertNotNull(codigo);
        assertEquals(5, codigo.length());
        assertTrue(codigo.matches("\\d{5}"));
    }

    // ================= CREAR SOLICITUD =================

    @Test
    void crearSolicitud_ok() {
        when(repo.save(any(PasswordReset.class)))
                .thenAnswer(i -> i.getArgument(0));

        PasswordReset reset = service.crearSolicitud("test@correo.com");

        assertEquals("test@correo.com", reset.getEmail());
        assertNotNull(reset.getRecoveryCode());
        assertNotNull(reset.getCreatedAt());
        assertNotNull(reset.getExpiresAt());

        assertTrue(reset.getExpiresAt()
                .isAfter(reset.getCreatedAt()));

        verify(repo).save(any(PasswordReset.class));
    }

    // ================= OBTENER ÚLTIMA =================

    @Test
    void obtenerUltimaSolicitud_ok() {
        PasswordReset reset = new PasswordReset();
        reset.setEmail("test@correo.com");

        when(repo.findTopByEmailOrderByCreatedAtDesc("test@correo.com"))
                .thenReturn(reset);

        PasswordReset result = service.obtenerUltimaSolicitud("test@correo.com");

        assertNotNull(result);
        assertEquals("test@correo.com", result.getEmail());
    }

    // ================= VALIDAR CÓDIGO =================

    @Test
    void validarCodigo_ok() {
        PasswordReset reset = new PasswordReset();
        reset.setEmail("test@correo.com");
        reset.setRecoveryCode("12345");
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(repo.findByEmailAndRecoveryCode("test@correo.com", "12345"))
                .thenReturn(reset);

        boolean valido = service.validarCodigo("test@correo.com", "12345");

        assertTrue(valido);
    }

    @Test
    void validarCodigo_noExiste() {
        when(repo.findByEmailAndRecoveryCode(anyString(), anyString()))
                .thenReturn(null);

        boolean valido = service.validarCodigo("test@correo.com", "00000");

        assertFalse(valido);
    }

    @Test
    void validarCodigo_expirado() {
        PasswordReset reset = new PasswordReset();
        reset.setEmail("test@correo.com");
        reset.setRecoveryCode("12345");
        reset.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repo.findByEmailAndRecoveryCode("test@correo.com", "12345"))
                .thenReturn(reset);

        boolean valido = service.validarCodigo("test@correo.com", "12345");

        assertFalse(valido);
    }
}
