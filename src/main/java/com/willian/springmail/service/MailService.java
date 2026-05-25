package com.willian.springmail.service;

import com.willian.springmail.auth.GmailService;
import com.willian.springmail.config.MailProperties;
import com.willian.springmail.dto.ContactRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MailService {
    private final GmailService gmailService;
    private final MailProperties mailProperties;

    public MailService(GmailService gmailService, MailProperties mailProperties) {
        this.gmailService = gmailService;
        this.mailProperties = mailProperties;
    }

    private boolean send(String from, String to, String subject, String bodyHtml) {
        if (to == null || to.isBlank()) {
            System.err.println("[EMAIL] Destinatário inválido ou vazio.");
            return false;
        }

        try {
            gmailService.sendMail(from, to, subject, bodyHtml);
            return true;
        } catch (Exception e) {
            System.err.println("[EMAIL] Falha ao enviar para: " + to + " | " + e.getMessage());
            return false;
        }
    }

    public boolean sendNotification(ContactRequest request) {
        String subject = "📍Novo contato do portfólio";
        String body = """
                <h2>Nova mensagem recebida</h2>
                <p><b>Nome:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>WhatsApp:</b> %s</p>
                <p><b>Mensagem:</b> %s</p>
                """.formatted(request.getName(), request.getMail(), request.getPhone(), request.getMessage());

        return send(mailProperties.getFrom(), mailProperties.getAdminMail(), subject, body);
    }

    public boolean sendReplyAutomatic(ContactRequest request) {
        String subject = "✅ Recebemos sua mensagem!";
        String body = """
                <h2>Olá %s,</h2>
                <p>Obrigado por entrar em contato! </p>
                <p>Recebemos sua mensagem e responderemos em breve!</p>
                <p>Atenciosamente,<br><b>Willian Rosa - Developer</b></p>
                """.formatted(request.getName());

        return send(mailProperties.getFrom(), request.getMail(), subject, body);
    }

    @Async
    public CompletableFuture<Boolean> mailValidation(ContactRequest request) {
        boolean adminOk = sendNotification(request);
        boolean userOK = sendReplyAutomatic(request);

        if (adminOk && userOK) {
            System.out.println("[EMAIL] Emails enviado com sucesso...");
        } else {
            System.err.println("[EMAIL] Falha parcial do envio - admin: " + adminOk + " | user: " + userOK);
        }

        return CompletableFuture.completedFuture(adminOk && userOK);
    }
}
