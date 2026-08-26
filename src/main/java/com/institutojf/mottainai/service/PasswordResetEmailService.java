package com.institutojf.mottainai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envia o código de recuperação para o email do usuário
 */
@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public PasswordResetEmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendRecoveryCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Mottainai password recovery");
        message.setText("Your recovery code is: " + code + "\n\nThis code expires in 10 minutes.");
        mailSender.send(message);
    }
}
