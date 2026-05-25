package com.willian.springmail.auth;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class GmailService {
    private final JavaMailSender mailSender;

    public GmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String from, String to, String subject, String bodyHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(bodyHtml, true);

            mailSender.send(message);
            System.out.println("[GMAIL] Email enviado com sucesso para: " + to);
        } catch (Exception e) {
            System.err.println("[GMAIL] Erro ao enviar email para: " + to + " | motivo: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar email para: " + to, e);
        }
    }
}
