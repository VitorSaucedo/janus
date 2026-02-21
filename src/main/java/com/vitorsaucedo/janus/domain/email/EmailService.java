package com.vitorsaucedo.janus.domain.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${janus.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Janus - Password Reset Request");
        message.setText("""
                You requested a password reset.
                
                Use the token below to reset your password:
                
                %s
                
                This token expires in 15 minutes.
                If you did not request this, please ignore this email.
                """.formatted(token));

        mailSender.send(message);
    }
}

