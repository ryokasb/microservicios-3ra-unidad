package com.example.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setup() {
        // Simula el correo configurado en application.properties
        ReflectionTestUtils.setField(
                emailService,
                "correoEmisor",
                "noreply@duodeal.com"
        );
    }

    // ================= ENVÍO OK =================

    @Test
    void enviarCorreo_ok() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
                emailService.enviarCorreo(
                        "test@correo.com",
                        "Asunto test",
                        "<h1>Mensaje</h1>"
                )
        );

        verify(mailSender).send(mimeMessage);
    }

    // ================= ERROR =================

   @Test
   void enviarCorreo_errorLanzaRuntimeException() throws MessagingException {
    when(mailSender.createMimeMessage())
            .thenThrow(new RuntimeException("No se pudo enviar el correo"));

    RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> emailService.enviarCorreo(
                    "test@correo.com",
                    "Asunto",
                    "Mensaje"
            )
    );

    assertEquals("No se pudo enviar el correo", ex.getMessage());

    verify(mailSender, never()).send(any(MimeMessage.class));
}

}
