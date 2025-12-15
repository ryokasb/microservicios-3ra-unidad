package com.example.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String correoEmisor;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreo(String correoDestino, String asunto, String mensaje) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(correoEmisor); 
            helper.setTo(correoDestino);
            helper.setSubject(asunto);
            helper.setText(mensaje, true); 

            mailSender.send(mimeMessage);

            System.out.println("Correo enviado a: " + correoDestino);

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo");
        }
    }
}
